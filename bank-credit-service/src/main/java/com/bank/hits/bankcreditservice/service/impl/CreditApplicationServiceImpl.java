package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.exception.CreditCreationException;
import com.bank.hits.bankcreditservice.model.CreditHistory;
import com.bank.hits.bankcreditservice.model.CreditTariff;
import com.bank.hits.bankcreditservice.model.DTO.CreditApplicationRequestDTO;
import com.bank.hits.bankcreditservice.model.DTO.CreditApplicationResponseDTO;
import com.bank.hits.bankcreditservice.model.DTO.CreditApprovedDTO;
import com.bank.hits.bankcreditservice.model.DTO.CreditClientInfoResponseDTO;
import com.bank.hits.bankcreditservice.model.DTO.PageInfoDTO;
import com.bank.hits.bankcreditservice.model.DTO.UserLoansResponseDTO;
import com.bank.hits.bankcreditservice.repository.CreditHistoryRepository;
import com.bank.hits.bankcreditservice.repository.CreditTariffRepository;
import com.bank.hits.bankcreditservice.service.api.CreditApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditApplicationServiceImpl implements CreditApplicationService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, SemaphoreResponsePair> semaphoreMap = new ConcurrentHashMap<>();

    private final CreditTariffRepository creditTariffRepository;

    private final CreditHistoryRepository creditHistoryRepository;

    @Value("${kafka.topics.core-information.request}")
    private String creditClientInfoRequestTopic;

    @Value("${kafka.topics.core-information.response}")
    private String creditClientInfoResponseTopic;

    @Value("${kafka.topics.approve}")
    private String creditApprovedTopic;

    @Override
    @Transactional
    public CreditApplicationResponseDTO processApplication(CreditApplicationRequestDTO request, String clientUuid) throws Exception {
        Optional<CreditTariff> tariffOpt = creditTariffRepository.findById(request.getTariffId());
        if (tariffOpt.isEmpty()) {
            throw new RuntimeException("Тариф не найден");
        }
        CreditTariff tariff = tariffOpt.get();

        String correlationId = sendClientInfoRequest(clientUuid);
        Semaphore semaphore = semaphoreMap.get(correlationId).getSemaphore();
        boolean acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
        if (!acquired) {
            throw new RuntimeException("Timeout waiting for client info response");
        }
        SemaphoreResponsePair pair = semaphoreMap.remove(correlationId);
        if (pair == null || pair.getResponse() == null) {
            throw new RuntimeException("No valid response received for client info");
        }
        CreditClientInfoResponseDTO clientInfo = objectMapper.readValue(pair.getResponse(), CreditClientInfoResponseDTO.class);
        boolean approved = evaluateCreditApplication(request, clientInfo);
        if(!approved)
        {
            throw new CreditCreationException("Вам отказано в кредите");
        }

        CreditApplicationResponseDTO responseDTO = new CreditApplicationResponseDTO();
        responseDTO.setNumber(request.getBankAccountNumber());

        CreditApplicationResponseDTO.TariffDTO tariffDTO = new CreditApplicationResponseDTO.TariffDTO();
        tariffDTO.setId(tariff.getId());
        tariffDTO.setName(tariff.getName());
        tariffDTO.setInterestRate(tariff.getInterestRate());
        tariffDTO.setCreatedAt(tariff.getCreatedAt().toLocalDateTime());
        responseDTO.setTariff(tariffDTO);

        responseDTO.setAmount(request.getAmount());
        responseDTO.setTermInMonths(request.getTermInMonths());
        responseDTO.setBankAccountNumber(request.getBankAccountNumber());

        BigDecimal monthlyPayment = calculateMonthlyPayment(request.getAmount(), tariff.getInterestRate(), request.getTermInMonths());
        responseDTO.setPaymentAmount(monthlyPayment);

        BigDecimal totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(request.getTermInMonths()));
        responseDTO.setPaymentSum(totalPayment);

        responseDTO.setNextPaymentDateTime(LocalDateTime.now().plusMonths(1));
        responseDTO.setCurrentDebt(request.getAmount().add(totalPayment.subtract(request.getAmount())));

        CreditHistory creditHistory = new CreditHistory();
        creditHistory.setTariffId(tariff.getId());
        creditHistory.setClientUuid(UUID.fromString(clientUuid));
        creditHistory.setTotalAmount(request.getAmount());
        creditHistory.setMonthlyPayment(monthlyPayment);
        creditHistory.setEndDate(LocalDateTime.now().plusMonths(request.getTermInMonths()));
        creditHistory.setRemainingDebt(totalPayment);
        creditHistory.setLoanNumber(request.getBankAccountNumber());
        creditHistoryRepository.save(creditHistory);
        sendCreditApprovedEvent(creditHistory);
        return responseDTO;
    }


    @Override
    public UserLoansResponseDTO getUserLoans(String clientUuid, int pageSize, int pageNumber) {
        UUID uuid = UUID.fromString(clientUuid);
        Page<CreditHistory> page = creditHistoryRepository.findByClientUuidAndRemainingDebtGreaterThan(uuid,BigDecimal.ZERO, PageRequest.of(pageNumber, pageSize));

        List<UserLoansResponseDTO.LoanDTO> loans = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageInfoDTO pageInfo = new PageInfoDTO(pageSize, pageNumber);
        UserLoansResponseDTO response = new UserLoansResponseDTO();
        response.setLoans(loans);
        response.setPageInfo(pageInfo);

        return response;
    }

    public UserLoansResponseDTO.LoanDTO getCreditByNumber(String number)
    {
        Optional<CreditHistory> credit = creditHistoryRepository.findByLoanNumber(number);
        if(!credit.isPresent())
        {
            throw new NoSuchElementException("Не удалось найти тариф с указанным номером");
        }
        CreditHistory request = credit.get();
        /*
        Optional<CreditTariff> tariffOpt = creditTariffRepository.findById(request.getTariffId());
        if (tariffOpt.isEmpty()) {
            throw new RuntimeException("Тариф не найден");
        }
        CreditTariff tariff = tariffOpt.get();

        CreditApplicationResponseDTO responseDTO = new CreditApplicationResponseDTO();
        responseDTO.setNumber(request.getLoanNumber());
        CreditApplicationResponseDTO.TariffDTO tariffDTO = new CreditApplicationResponseDTO.TariffDTO();
        tariffDTO.setId(tariff.getId());
        tariffDTO.setName(tariff.getName());
        tariffDTO.setInterestRate(tariff.getInterestRate());
        tariffDTO.setCreatedAt(tariff.getCreatedAt().toLocalDateTime());

        Integer monthsBetween = Math.toIntExact(ChronoUnit.MONTHS.between(request.getStartDate(), request.getEndDate()));

        responseDTO.setTariff(tariffDTO);

        responseDTO.setAmount(request.getTotalAmount());
        responseDTO.setTermInMonths(monthsBetween);
        responseDTO.setBankAccountNumber(request.getLoanNumber());

        BigDecimal monthlyPayment = calculateMonthlyPayment(request.getTotalAmount(), tariff.getInterestRate(), monthsBetween);
        responseDTO.setPaymentAmount(monthlyPayment);

        BigDecimal totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(monthsBetween));
        responseDTO.setPaymentSum(totalPayment);

        responseDTO.setNextPaymentDateTime(LocalDateTime.now().plusMonths(1));
        responseDTO.setCurrentDebt(request.getTotalAmount().add(totalPayment.subtract(BigDecimal.valueOf(monthsBetween))));
        return responseDTO;

         */
        UserLoansResponseDTO.LoanDTO response = convertToDTO(request);
        return response;
    }

    private UserLoansResponseDTO.LoanDTO convertToDTO(CreditHistory credit) {
        UserLoansResponseDTO.LoanDTO loanDTO = new UserLoansResponseDTO.LoanDTO();
        loanDTO.setNumber(credit.getLoanNumber());
        loanDTO.setAmount(credit.getTotalAmount());
        loanDTO.setTermInMonths((int) credit.getEndDate().minusMonths(credit.getStartDate().getMonthValue()).getMonthValue());
        loanDTO.setBankAccountNumber(credit.getLoanNumber());
        loanDTO.setPaymentAmount(credit.getMonthlyPayment());
        loanDTO.setPaymentSum(credit.getMonthlyPayment().multiply(BigDecimal.valueOf(loanDTO.getTermInMonths())));
        loanDTO.setNextPaymentDateTime(credit.getStartDate().plusMonths(1));
        loanDTO.setCurrentDebt(credit.getRemainingDebt());

        CreditTariff tariff = creditTariffRepository.findById(credit.getTariffId()).orElse(null);
        if (tariff != null) {
            UserLoansResponseDTO.TariffDTO tariffDTO = new UserLoansResponseDTO.TariffDTO();
            tariffDTO.setId(tariff.getId());
            tariffDTO.setName(tariff.getName());
            tariffDTO.setInterestRate(BigDecimal.valueOf(tariff.getInterestRate()));
            tariffDTO.setCreatedAt(tariff.getCreatedAt().toLocalDateTime());
            loanDTO.setTariff(tariffDTO);
        }

        return loanDTO;
    }

    private void sendCreditApprovedEvent(CreditHistory creditHistory) {
        try {
            CreditApprovedDTO approvedDto = new CreditApprovedDTO();
            approvedDto.setClientId(creditHistory.getClientUuid());
            approvedDto.setApprovedAmount(creditHistory.getTotalAmount());
            approvedDto.setRemainingAmount(creditHistory.getRemainingDebt());
            approvedDto.setApprovedDate(LocalDateTime.now());
            approvedDto.setStartDate(creditHistory.getStartDate());
            approvedDto.setEndDate(creditHistory.getEndDate());

            String message = objectMapper.writeValueAsString(approvedDto);
            ProducerRecord<String, String> record = new ProducerRecord<>(creditApprovedTopic, message);
            kafkaTemplate.send(record);
            log.info("Сообщение о подтверждении кредита отправлено в Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при сериализации CreditApprovedDto", e);
        }
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, double interestRate, int months) {
        BigDecimal monthlyRate = BigDecimal.valueOf(interestRate).divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRatePowerN = BigDecimal.ONE.add(monthlyRate).pow(months);
        return amount.multiply(monthlyRate).multiply(onePlusRatePowerN)
                .divide(onePlusRatePowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }
    private boolean evaluateCreditApplication(CreditApplicationRequestDTO request, CreditClientInfoResponseDTO clientInfo) {
        boolean hasActiveAccount = clientInfo.getAccounts().stream()
                .anyMatch(account -> !account.isBlocked() && !account.isClosed());
        boolean creditWithinLimit = clientInfo.getCreditHistory().getTotalCreditAmount().compareTo(request.getAmount()) < 0;
        return hasActiveAccount && creditWithinLimit;
    }

    private String sendClientInfoRequest(String clientUuid) throws JMSException {
        String correlationId = UUID.randomUUID().toString();
        ProducerRecord<String, String> record = new ProducerRecord<>(creditClientInfoRequestTopic, clientUuid);
        record.headers().add("event_type", "get_credit_client_info".getBytes());
        record.headers().add("client_uuid", clientUuid.getBytes());
        record.headers().add("correlation_id", correlationId.getBytes());
        record.headers().add("timeoutExpire", "30".getBytes());
        kafkaTemplate.send(record);
        semaphoreMap.put(correlationId, new SemaphoreResponsePair(new Semaphore(0), null));
        return correlationId;
    }

    @KafkaListener(topics = "${kafka.topics.core-information.response}", groupId = "creditClientInfoGroup")
    public void receiveClientInfoResponse(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получен ответ без заголовка correlation_id");
            return;
        }
        String correlationId = new String(header.value());
        String response = record.value();
        SemaphoreResponsePair pair = semaphoreMap.get(correlationId);
        if (pair != null) {
            pair.setResponse(response);
            pair.getSemaphore().release();
        } else {
            log.warn("Не найден ожидающий поток для correlationId: " + correlationId);
        }
    }
    private static class SemaphoreResponsePair {
        private Semaphore semaphore;
        private String response;

        public SemaphoreResponsePair(Semaphore semaphore, String response) {
            this.semaphore = semaphore;
            this.response = response;
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}
