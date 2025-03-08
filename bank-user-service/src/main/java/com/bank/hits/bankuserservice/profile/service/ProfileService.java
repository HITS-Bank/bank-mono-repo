package com.bank.hits.bankuserservice.profile.service;

import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.exception.ForbiddenActionException;
import com.bank.hits.bankuserservice.common.exception.IncorrectActionException;
import com.bank.hits.bankuserservice.common.exception.InitiatorUserNotFoundException;
import com.bank.hits.bankuserservice.common.exception.UserNotFoundException;
import com.bank.hits.bankuserservice.common.exception.message.UserServiceExceptionMessage;
import com.bank.hits.bankuserservice.common.mapper.UserMapper;
import com.bank.hits.bankuserservice.common.model.UserEntity;
import com.bank.hits.bankuserservice.common.repository.UserRepository;
import com.bank.hits.bankuserservice.kafka.service.KafkaProfileService;
import com.bank.hits.bankuserservice.profile.dto.UserListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProfileService kafkaProfileService;

    @Transactional
    public void banUser(UUID employeeId, UUID userId) {
        UserEntity initiator = userRepository
                .findById(employeeId)
                .orElseThrow(() -> new InitiatorUserNotFoundException(UserServiceExceptionMessage.INITIATOR_NOT_FOUND));

        if (initiator.getIsBanned()) {
            throw new IllegalStateException(UserServiceExceptionMessage.INITIATOR_BANNED);
        }

        UserEntity userToBan = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserServiceExceptionMessage.USER_NOT_FOUND));

        if (userToBan.getIsBanned()) {
            throw new IncorrectActionException(UserServiceExceptionMessage.USER_ALREADY_BANNED);
        }

        userToBan.setIsBanned(true);

        userRepository.save(userToBan);

        if (userToBan.getRole().equals(UserEntity.Role.CLIENT)) {
            kafkaProfileService.sendClientBanned(userToBan.getId());
        } else {
            kafkaProfileService.sendEmployeeBanned(userToBan.getId());
        }
    }

    @Transactional
    public void unbanUser(UUID employeeId, UUID userId) {
        UserEntity initiator = userRepository
                .findById(employeeId)
                .orElseThrow(() -> new InitiatorUserNotFoundException(UserServiceExceptionMessage.INITIATOR_NOT_FOUND));

        if (initiator.getIsBanned()) {
            throw new ForbiddenActionException(UserServiceExceptionMessage.INITIATOR_BANNED);
        }

        UserEntity userToUnban = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserServiceExceptionMessage.USER_NOT_FOUND));

        if (!userToUnban.getIsBanned()) {
            throw new IncorrectActionException(UserServiceExceptionMessage.USER_NOT_BANNED);
        }

        userToUnban.setIsBanned(false);
        userRepository.save(userToUnban);

        if (userToUnban.getRole().equals(UserEntity.Role.CLIENT)) {
            kafkaProfileService.sendClientUnbanned(userToUnban.getId());
        } else {
            kafkaProfileService.sendEmployeeUnbanned(userToUnban.getId());
        }
    }

    public List<UserDto> getUserList(UUID employeeId, UserListRequest request) {
        UserEntity initiator = userRepository
                .findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException(UserServiceExceptionMessage.USER_NOT_FOUND));

        if (initiator.getIsBanned()) {
            throw new ForbiddenActionException(UserServiceExceptionMessage.INITIATOR_BANNED);
        }

        UserEntity.Role role = request.role();
        String nameQuery = request.nameQuery();
        int pageSize = request.pageSize();
        int pageNumber = request.pageNumber();

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        Page<UserEntity> userPage;

        if (StringUtils.hasText(role.name()) && StringUtils.hasText(nameQuery)) {
            userPage = userRepository.findByRoleAndFirstNameContainingIgnoreCase(role, nameQuery, pageRequest);
        }
        else if (StringUtils.hasText(role.name())) {
            userPage = userRepository.findByRole(role, pageRequest);
        }
        else if (StringUtils.hasText(nameQuery)) {
            userPage = userRepository.findByFirstNameContainingIgnoreCase(nameQuery, pageRequest);
        }
        else {
            userPage = userRepository.findAll(pageRequest);
        }

        return userPage.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
