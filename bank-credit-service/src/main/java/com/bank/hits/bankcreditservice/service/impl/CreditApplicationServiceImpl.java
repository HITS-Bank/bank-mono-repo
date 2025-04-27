package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.exception.CreditCreationException;
import com.bank.hits.bankcreditservice.model.CreditHistory;
import com.bank.hits.bankcreditservice.model.CreditTariff;
import com.bank.hits.bankcreditservice.model.DTO.*;
import com.bank.hits.bankcreditservice.repository.CreditHistoryRepository;
import com.bank.hits.bankcreditservice.repository.CreditTariffRepository;
import com.bank.hits.bankcreditservice.service.api.CreditApplicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
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
import java.security.SecureRandom;
import java.time.LocalDateTime;
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

    private static final int LENGTH = 16;

    private static final SecureRandom random = new SecureRandom();

    private final Map<String, SemaphoreResponsePair> semaphoreMap = new ConcurrentHashMap<>();

    private final CreditTariffRepository creditTariffRepository;

    private final CreditHistoryRepository creditHistoryRepository;

    @Value("${kafka.topics.core-information.request}")
    private String creditClientInfoRequestTopic;

    @Value("${kafka.topics.core-information.response}")
    private String creditClientInfoResponseTopic;

    @Value("${kafka.topics.approve.request}")
    private String creditApprovedTopic;

    @Override
    @Transactional
    public CreditApplicationResponseDTO processApplication(CreditApplicationRequestDTO request, String clientUuid) throws Exception {
        CreditTariff tariff = creditTariffRepository.findById(request.getTariffId())
                .orElseThrow(() -> new RuntimeException("Тариф не найден"));
        CreditClientInfoResponseDTO clientInfo = getClientInfoWithResilience(clientUuid);

        boolean approved = approveCredit(request, clientInfo);
        if (!approved) throw new CreditCreationException("Вам отказано в кредите");
        CreditApplicationResponseDTO responseDTO = buildResponseDTO(request, tariff, clientUuid);
        boolean approveCredit = sendCreditApprovalWithResilience(responseDTO, request, clientUuid);
        if (!approveCredit) throw new CreditCreationException("Не удалось получить кредит, попробуйте позже");

        creditHistoryRepository.save(mapToHistory(request, clientUuid, responseDTO));

        return responseDTO;
    }

    public static String generateNumericString() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }



    @CircuitBreaker(name = "clientInfoService", fallbackMethod = "clientInfoFallback")
    @Retry(name = "clientInfoService")
    private CreditClientInfoResponseDTO getClientInfoWithResilience(String clientUuid) throws Exception {
        String correlationId = sendClientInfoRequest(clientUuid);
        Semaphore semaphore = semaphoreMap.get(correlationId).getSemaphore();
        if (!semaphore.tryAcquire(30, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for client info response");
        }
        SemaphoreResponsePair pair = semaphoreMap.remove(correlationId);
        return objectMapper.readValue(pair.getResponse(), CreditClientInfoResponseDTO.class);
    }

    private CreditClientInfoResponseDTO clientInfoFallback(String clientUuid, Throwable t) {
        throw new RuntimeException("Сервис получения информации о клиенте временно недоступен", t);
    }



    @CircuitBreaker(name = "creditApprovalService", fallbackMethod = "creditApprovalFallback")
    @Retry(name = "creditApprovalService")
    private boolean sendCreditApprovalWithResilience(CreditApplicationResponseDTO responseDTO, CreditApplicationRequestDTO request, String clientUUID) throws Exception {
        // ваш код sendCreditApprovedEvent + ожидание семафора
        String correlationId = sendCreditApprovedEvent(mapToHistory(request,clientUUID, responseDTO));
        Semaphore sem = semaphoreMap.get(correlationId).getSemaphore();
        if (!sem.tryAcquire(30, TimeUnit.SECONDS))
            throw new RuntimeException("Timeout waiting for credit approval response");
        SemaphoreResponsePair pair = semaphoreMap.remove(correlationId);
        return objectMapper.readValue(pair.getResponse(), boolean.class);
    }

    private boolean creditApprovalFallback(CreditApplicationResponseDTO dto, CreditApplicationRequestDTO req, Throwable t) {
        throw new RuntimeException("Сервис одобрения кредита временно недоступен", t);
    }


    @Override
    public UserLoansResponseDTO getUserLoans(String clientUuid, int pageSize, int pageNumber) {
        UUID uuid = UUID.fromString(clientUuid);
        log.info("uuid = " + uuid);
        Page<CreditHistory> page = creditHistoryRepository.findByClientUuidAndRemainingDebtGreaterThan(uuid,BigDecimal.ZERO, PageRequest.of(pageNumber - 1, pageSize));
        log.info("page = " + page);

        List<UserLoansResponseDTO.LoanDTO> loans = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PageInfoDTO pageInfo = new PageInfoDTO(pageSize, pageNumber);
        UserLoansResponseDTO response = new UserLoansResponseDTO();
        response.setLoans(loans);
        //response.setPageInfo(pageInfo);

        return response;
    }

    public UserLoansResponseDTO.LoanDTO getCreditById(UUID id)
    {
        Optional<CreditHistory> credit = creditHistoryRepository.findById(id);
        if(!credit.isPresent())
        {
            throw new NoSuchElementException("Не удалось найти кредит с указанным id");
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
        loanDTO.setId(credit.getId());
        loanDTO.setCurrencyCode(CurrencyCode.RUB);
        loanDTO.setNumber(credit.getNumber());
        loanDTO.setAmount(credit.getTotalAmount());
        loanDTO.setTermInMonths((int) credit.getEndDate().minusMonths(credit.getStartDate().getMonthValue()).getMonthValue());
        loanDTO.setBankAccountNumber(credit.getBankAccountNumber());
        loanDTO.setBankAccountId(credit.getBankAccountId());
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

    private String sendCreditApprovedEvent(CreditHistory creditHistory) {
        String correlationId = UUID.randomUUID().toString();
        try {
            CreditApprovedDTO approvedDto = new CreditApprovedDTO();
            log.info("creditId = " + creditHistory.getId());
            approvedDto.setCreditId(creditHistory.getId());
            approvedDto.setAccountId(creditHistory.getBankAccountId());
            approvedDto.setClientId(creditHistory.getClientUuid());
            approvedDto.setApprovedAmount(creditHistory.getTotalAmount());
            approvedDto.setRemainingAmount(creditHistory.getRemainingDebt());
            approvedDto.setApprovedDate(LocalDateTime.now());
            approvedDto.setStartDate(creditHistory.getStartDate());
            approvedDto.setEndDate(creditHistory.getEndDate());

            String message = objectMapper.writeValueAsString(approvedDto);
            ProducerRecord<String, String> record = new ProducerRecord<>(creditApprovedTopic, message);
            record.headers().add("correlation_id", correlationId.getBytes());
            log.info("record подтверждения кредита: {}", record);
            kafkaTemplate.send(record);
            log.info("Сообщение о подтверждении кредита отправлено в Kafka: {}", message);
            semaphoreMap.put(correlationId, new SemaphoreResponsePair(new Semaphore(0), null));
        } catch (JsonProcessingException e) {
            log.error("Ошибка при сериализации CreditApprovedDto", e);
        }
        return correlationId;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, double interestRate, int months) {
        BigDecimal monthlyRate = BigDecimal.valueOf(interestRate).divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRatePowerN = BigDecimal.ONE.add(monthlyRate).pow(months);
        return amount.multiply(monthlyRate).multiply(onePlusRatePowerN)
                .divide(onePlusRatePowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    public boolean approveCredit(CreditApplicationRequestDTO request, CreditClientInfoResponseDTO clientInfo) {
        BigDecimal masterAmount = clientInfo.getMasterAccountAmount();
        if (masterAmount.compareTo(request.getAmount()) < 0) {
            log.info("На счету банка недостаточно средств для выдачи кредита");
            return false;
        }
        if (hasOverdueCredits(clientInfo.getCredits())) {
            log.info("Клиент имеет просроченные кредиты");
            return false;
        }
        if (!isAccountValid(request.getBankAccountNumber(), clientInfo.getAccounts())) {
            log.info("Невалидный банковский счет");
            log.info("request.getBankAccountNumber() = {}", request.getBankAccountNumber());
            log.info("clientInfo.getAccounts() = {}", clientInfo.getAccounts());
            return false;
        }
        if (isCreditLoadTooHigh(clientInfo.getCredits(), request.getAmount())) {
            log.info("Долговая нагрузка слишком высока");
            return false;
        }
        int creditRating = clientInfo.getCreditRating();
        int requiredMinimumCreditRating = 5;
        if (creditRating < requiredMinimumCreditRating) {
            log.info("Низкий кредитный рейтинг: {}. Минимально допустимый рейтинг: {}", creditRating, requiredMinimumCreditRating);
            return false;
        }

        // допустимый лимит = рейтинг * 1000.
        BigDecimal allowedCreditLimit = BigDecimal.valueOf(creditRating).multiply(new BigDecimal("1000"));
        if (request.getAmount().compareTo(allowedCreditLimit) > 0) {
            log.info("Запрошенная сумма {} превышает кредитный лимит {} (основанный на кредитном рейтинге {})", request.getAmount(), allowedCreditLimit, creditRating);
            return false;
        }


        return true;
    }

    private boolean hasOverdueCredits(List<CreditContractDto> credits) {
        LocalDateTime now = LocalDateTime.now();
        for (CreditContractDto credit : credits) {
            if (credit.getCreditRepaymentAmount() != null && credit.getCreditRepaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
                return true; // Есть просроченные кредиты
            }
        }
        return false;
    }

    private boolean isAccountValid(String accountNumber, List<AccountInfoDTO> accounts) {
        for (AccountInfoDTO account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return !account.isBlocked() && !account.isClosed() && new BigDecimal(account.getBalance()).compareTo(BigDecimal.ZERO) > 0;
            }
        }
        return false;
    }

    private boolean isCreditLoadTooHigh(List<CreditContractDto> credits, BigDecimal requestedAmount) {
        BigDecimal totalDebt = BigDecimal.ZERO;
        for (CreditContractDto credit : credits) {
            totalDebt = totalDebt.add(credit.getCreditAmount());
        }

        BigDecimal debtLimit = new BigDecimal("50000"); // Условный порог долговой нагрузки
        return totalDebt.add(requestedAmount).compareTo(debtLimit) > 0;
    }

    private String sendClientInfoRequest(String clientUuid) throws JMSException {
        log.info("Id клиента {}", clientUuid);
        String correlationId = UUID.randomUUID().toString();
        ProducerRecord<String, String> record = new ProducerRecord<>(creditClientInfoRequestTopic, clientUuid);
        record.headers().add("event_type", "get_credit_client_info".getBytes());
        record.headers().add("client_uuid", clientUuid.getBytes());
        record.headers().add("correlation_id", correlationId.getBytes());
        record.headers().add("timeoutExpire", "30".getBytes());

        log.info("record {}", record);
        kafkaTemplate.send(record);
        log.info("Отправлен запрос на получение информации о клиенте: {}", clientUuid);
        semaphoreMap.put(correlationId, new SemaphoreResponsePair(new Semaphore(0), null));

        return correlationId;
    }

    @KafkaListener(topics = "${kafka.topics.core-information.response}", groupId = "creditClientInfoGroup")
    public void receiveClientInfoResponse(ConsumerRecord<String, String> record) {
        log.info("Получено сообщение clientInfo");
        Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получен ответ без заголовка correlation_id");
            return;
        }
        String correlationId = new String(header.value());
        log.info("correlationId {}", correlationId);
        String response = record.value();
        SemaphoreResponsePair pair = semaphoreMap.get(correlationId);
        if (pair != null) {
            pair.setResponse(response);
            pair.getSemaphore().release();
        } else {
            log.warn("Не найден ожидающий поток для correlationId: " + correlationId);
        }
    }


    @KafkaListener(topics = "${kafka.topics.approve.response}", groupId = "creditClientInfoGroup")
    public void receiveCreditApproveResponse(ConsumerRecord<String, String> record) {
        log.info("Получено сообщение одобрения кредита");
        Header header = record.headers().lastHeader("correlation_id");
        if (header == null) {
            log.warn("Получен ответ без заголовка correlation_id");
            return;
        }
        String correlationId = new String(header.value());
        log.info("correlationId {}", correlationId);
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


    private CreditApplicationResponseDTO buildResponseDTO(CreditApplicationRequestDTO request,
                                                          CreditTariff tariff,
                                                          String clientUuid) {
        // генерируем id и номер кредита
        UUID creditId = UUID.randomUUID();
        String creditNumber = generateNumericString();

        // рассчитываем платежи
        BigDecimal monthlyPayment = calculateMonthlyPayment(request.getAmount(),
                tariff.getInterestRate(),
                request.getTermInMonths());
        BigDecimal totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(request.getTermInMonths()));

        // таррифт в ответе
        CreditApplicationResponseDTO.TariffDTO tariffDTO = new CreditApplicationResponseDTO.TariffDTO();
        tariffDTO.setId(tariff.getId());
        tariffDTO.setName(tariff.getName());
        tariffDTO.setInterestRate(tariff.getInterestRate());
        tariffDTO.setCreatedAt(tariff.getCreatedAt().toLocalDateTime());

        // сам ответ
        CreditApplicationResponseDTO responseDTO = new CreditApplicationResponseDTO();
        responseDTO.setId(creditId);
        responseDTO.setNumber(creditNumber);
        responseDTO.setTariff(tariffDTO);
        responseDTO.setAmount(request.getAmount());
        responseDTO.setTermInMonths(request.getTermInMonths());
        responseDTO.setBankAccountNumber(request.getBankAccountNumber());
        responseDTO.setBankAccountId(request.getBankAccountId());
        responseDTO.setCurrencyCode(CurrencyCode.RUB);
        responseDTO.setPaymentAmount(monthlyPayment);
        responseDTO.setPaymentSum(totalPayment);
        responseDTO.setNextPaymentDateTime(LocalDateTime.now().plusMonths(1));
        responseDTO.setCurrentDebt(totalPayment);

        return responseDTO;
    }

    private CreditHistory mapToHistory(CreditApplicationRequestDTO request,
                                       String clientUuid,
                                       CreditApplicationResponseDTO responseDTO) {
        CreditHistory history = new CreditHistory();
        history.setId(responseDTO.getId());
        history.setTariffId(responseDTO.getTariff().getId());
        history.setClientUuid(UUID.fromString(clientUuid));
        history.setTotalAmount(responseDTO.getAmount());
        history.setMonthlyPayment(responseDTO.getPaymentAmount());
        history.setStartDate(LocalDateTime.now());
        history.setEndDate(LocalDateTime.now().plusMonths(responseDTO.getTermInMonths()));
        history.setRemainingDebt(responseDTO.getPaymentSum());
        history.setNumber(responseDTO.getNumber());
        history.setBankAccountNumber(responseDTO.getBankAccountNumber());
        history.setBankAccountId(responseDTO.getBankAccountId());
        return history;
    }
}
