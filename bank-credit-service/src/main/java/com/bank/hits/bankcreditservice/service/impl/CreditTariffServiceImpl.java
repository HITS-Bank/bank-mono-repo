package com.bank.hits.bankcreditservice.service.impl;

import com.bank.hits.bankcreditservice.model.CreditTariff;
import com.bank.hits.bankcreditservice.model.DTO.CreditTariffDTO;
import com.bank.hits.bankcreditservice.model.DTO.LoanTariffResponseDTO;
import com.bank.hits.bankcreditservice.model.DTO.PageInfoDTO;
import com.bank.hits.bankcreditservice.repository.CreditTariffRepository;
import com.bank.hits.bankcreditservice.service.api.CreditTariffService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditTariffServiceImpl implements CreditTariffService {
    private final CreditTariffRepository repository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY = "activeCreditTariffs";


    @Transactional
    @CacheEvict(value = "activeCreditTariffs", allEntries = true)
    public CreditTariff saveTariff(CreditTariff tariff) {
        return repository.save(tariff);
    }

    public Optional<CreditTariff> getTariffById(UUID id) {
        return repository.findById(id);
    }

    public CreditTariffDTO convertToDTO(CreditTariff tariff) {
        return modelMapper.map(tariff, CreditTariffDTO.class);
    }

    @Transactional
    @CacheEvict(value = "activeCreditTariffs", allEntries = true)
    public boolean markTariffAsInactive(UUID id) {
        Optional<CreditTariff> tariffOpt = repository.findById(id);
        if (tariffOpt.isPresent()) {
            CreditTariff tariff = tariffOpt.get();
            tariff.setRelevance(false);
            repository.save(tariff);
            return true;
        }
        return false;
    }

    @Cacheable(value = "activeCreditTariffs")
    public LoanTariffResponseDTO getActiveTariffs(
            String nameQuery, String sortingProperty, String sortingOrder, int pageSize, int pageNumber) {
        Sort.Direction direction = sortingOrder.equalsIgnoreCase("Descending") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, mapSortingProperty(sortingProperty));
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<CreditTariff> page = repository.findByRelevanceTrueAndNameContainingIgnoreCase(nameQuery, pageable);
        List<CreditTariffDTO> loanTariffs = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Формируем ответ
        PageInfoDTO pageInfo = new PageInfoDTO(pageSize, pageNumber);
        return new LoanTariffResponseDTO(loanTariffs, pageInfo);
    }

    private String mapSortingProperty(String property) {
        return switch (property.toLowerCase()) {
            case "created_at" -> "created_at";
            case "interest_rate" -> "interest_rate";
            default -> "name";
        };
    }
}
