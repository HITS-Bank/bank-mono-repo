package com.bank.hits.bankpersonalizationservice;

import com.bank.hits.bankpersonalizationservice.common.enums.Role;
import com.bank.hits.bankpersonalizationservice.model.dto.HiddenAccountsDto;
import com.bank.hits.bankpersonalizationservice.model.dto.ThemeDto;
import com.bank.hits.bankpersonalizationservice.model.entity.HiddenAccountEntity;
import com.bank.hits.bankpersonalizationservice.model.entity.UserThemeEntity;
import com.bank.hits.bankpersonalizationservice.repository.HiddenAccountsRepository;
import com.bank.hits.bankpersonalizationservice.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalizationService {

    private final ThemeRepository themeRepository;
    private final HiddenAccountsRepository hiddenAccountsRepository;

    public ThemeDto setTheme(
            String token,
            Role channel,
            ThemeDto themeDto
    ) {
        String userId = getUserId(token);

        Optional<UserThemeEntity> existingTheme = themeRepository.findByUserIdAndChannel(userId, channel);
        if (existingTheme.isPresent()) {
            UserThemeEntity entity = existingTheme.get();

            entity.setTheme(themeDto.getTheme());
            themeRepository.save(entity);

            return ThemeDto.builder().theme(entity.getTheme()).build();
        } else {
            UserThemeEntity entity = new UserThemeEntity();

            entity.setUserId(userId);
            entity.setChannel(channel);
            entity.setTheme(themeDto.getTheme());

            themeRepository.save(entity);

            return ThemeDto.builder().theme(entity.getTheme()).build();
        }
    }

    public ThemeDto getTheme(
            String token,
            Role channel
    ) {
        String userId = getUserId(token);

        UserThemeEntity entity = themeRepository.findByUserIdAndChannel(userId, channel)
                .orElseThrow(() -> new RuntimeException("Тема не установлена"));

        return ThemeDto.builder().theme(entity.getTheme()).build();
    }

    public void addHiddenAccount(
        String token,
        UUID accountId
    ) {
        String userId = getUserId(token);

        Optional<HiddenAccountEntity> existingHiddenAccount = hiddenAccountsRepository.findByUserIdAndAccountId(
                userId, accountId
        );
        if (existingHiddenAccount.isPresent()) {
            throw new RuntimeException("Такая запись уже есть");
        } else {
            HiddenAccountEntity entity = new HiddenAccountEntity();

            entity.setUserId(userId);
            entity.setAccountId(accountId);

            hiddenAccountsRepository.save(entity);
        }
    }

    public void deleteHiddenAccount(
            String token,
            UUID accountId
    ) {
        String userId = getUserId(token);

        Optional<HiddenAccountEntity> existingHiddenAccount = hiddenAccountsRepository.findByUserIdAndAccountId(
                userId, accountId
        );
        if (existingHiddenAccount.isEmpty()) {
            throw new RuntimeException("Такой записи нет");
        } else {
            hiddenAccountsRepository.delete(existingHiddenAccount.get());
        }
    }

    public HiddenAccountsDto getHiddenAccountList(
            String token
    ) {
        String userId = getUserId(token);

        List<HiddenAccountEntity> entities = hiddenAccountsRepository.findAllByUserId(userId);
        List<UUID> accounts = entities.stream()
                .map(HiddenAccountEntity::getAccountId)
                .collect(Collectors.toList());

        var dto = new HiddenAccountsDto();
        dto.setAccounts(accounts);

        return dto;
    }

    private String getUserId(String token) {
        try {
            return TokenVerifier.create(token, AccessToken.class).getToken().getSubject();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }
    }
}
