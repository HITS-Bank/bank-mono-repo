package com.bank.hits.bankuserservice.user_service.service;

import com.bank.hits.bankuserservice.auth.dto.RegisterRequest;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.enums.Role;
import com.bank.hits.bankuserservice.common.exception.ForbiddenActionException;
import com.bank.hits.bankuserservice.common.model.KeycloakRoleResponse;
import com.bank.hits.bankuserservice.common.model.KeycloakUserResponse;
import com.bank.hits.bankuserservice.kafka.service.KafkaProfileService;
import com.bank.hits.bankuserservice.profile.dto.UserListRequest;
import com.bank.hits.bankuserservice.user_service.mapper.UserMapper;
import com.bank.hits.bankuserservice.user_service.repository.KeycloakRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final KafkaProfileService kafkaProfileService;

    private final KeycloakRepository keycloakRepository;

    public UserDto getSelfUserProfile(String token) {
        String userId;
        try {
            userId = TokenVerifier.create(token, AccessToken.class).getToken().getSubject();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }

        KeycloakUserResponse user = keycloakRepository.getUser(userId);

        List<Role> userRoles = getRolesForUser(user.getId()).stream().toList();

        return userMapper.toDto(user, userRoles);
    }

    public UserDto getUserProfile(String token, String userId) {
        UserDto executorUser = getSelfUserProfile(token);
        if (executorUser.getIsBlocked()) {
            throw new ForbiddenActionException("Вызывающий забанен");
        }

        KeycloakUserResponse user = keycloakRepository.getUser(userId);

        List<Role> userRoles = getRolesForUser(user.getId()).stream().toList();

        return userMapper.toDto(user, userRoles);
    }

    public void banUser(String token, String userId) throws JsonProcessingException {
        UserDto executorUser = getSelfUserProfile(token);
        if (executorUser.getIsBlocked()) {
            throw new ForbiddenActionException("Вызывающий забанен");
        }

        if (Objects.equals(executorUser.getId(), userId)) {
            throw new ForbiddenActionException("Ты че, пытаешься себя забанить? Фрик?");
        }

        keycloakRepository.updateUserAttributes(
                userId,
                Collections.singletonMap("isBanned", Collections.singletonList("true"))
        );

        kafkaProfileService.coreSendUserBanned(userId);
    }

    public void unbanUser(String token, String userId) throws JsonProcessingException {
        UserDto executorUser = getSelfUserProfile(token);
        if (executorUser.getIsBlocked()) {
            throw new ForbiddenActionException("Вызывающий забанен");
        }

        keycloakRepository.updateUserAttributes(
                userId,
                Collections.singletonMap("isBanned", Collections.singletonList("false"))
        );

        kafkaProfileService.coreSendUserUnbanned(userId);
    }

    public void registerUser(String token, RegisterRequest request) {
        UserDto executorUser = getSelfUserProfile(token);
        if (executorUser.getIsBlocked()) {
            throw new ForbiddenActionException("Вызывающий забанен");
        }

        ResponseEntity<Void> response = keycloakRepository.createUser(request);

        String userId;
        if (response.getStatusCode() == HttpStatus.CREATED) {
            URI location = response.getHeaders().getLocation();
            if (location != null) {
                String locationString = location.toString();
                userId = locationString.substring(locationString.lastIndexOf('/') + 1);
            } else {
                throw new RuntimeException("Не удалось получить userId после создания пользователя");
            }
        } else {
            throw new RuntimeException("Не удалось получить userId после создания пользователя");
        }

        keycloakRepository.setPasswordForUser(userId, request.getPassword());

        List<KeycloakRoleResponse> rolesResponse = keycloakRepository.getKeycloakClientRoles();

        rolesResponse.forEach(roleResponse -> {
            log.info("got role response: " + roleResponse.getId() + " " + roleResponse.getName() + " " + roleResponse.getContainerId());
        });

        Map<String, String> availableRoles = rolesResponse.stream()
                .collect(Collectors.toMap(
                        role -> role.getName(),
                        KeycloakRoleResponse::getId
                ));

        List<Map<String, String>> rolesToAssign = request.getRoles().stream()
                .map(role -> role.name())
                .filter(availableRoles::containsKey)
                .map(roleName -> {
                    String roleId = availableRoles.get(roleName);
                    log.info("role to assign: " + roleId + " " + roleName);
                    return Map.of("id", roleId, "name", roleName);
                })
                .collect(Collectors.toList());

        if (rolesToAssign.isEmpty()) {
            throw new RuntimeException("Нет доступных ролей для назначения");
        }

        keycloakRepository.assignRolesForUser(userId, rolesToAssign);
    }

    public List<UserDto> getUserList(String token, UserListRequest request) {
        UserDto executorUser = getSelfUserProfile(token);
        if (executorUser.getIsBlocked()) {
            throw new ForbiddenActionException("Вызывающий забанен");
        }

        List<KeycloakUserResponse> users = keycloakRepository.getUsers(
                request.pageNumber(),
                request.pageSize(),
                request.role() != null ? request.role().name() : Role.EMPLOYEE.name()
        );

        if (request.nameQuery() != null && !request.nameQuery().isEmpty()) {
            String query = request.nameQuery().toLowerCase();
            users = users.stream()
                    .filter(user -> (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(query)) ||
                            (user.getLastName() != null && user.getLastName().toLowerCase().contains(query)))
                    .collect(Collectors.toList());
        }

        List<UserDto> userDtos = new ArrayList<>();

        users.forEach(user -> {
            List<Role> userRoles = getRolesForUser(user.getId()).stream().toList();
            userDtos.add(userMapper.toDto(user, userRoles));
        });

        return userDtos;
    }

    private Set<Role> getRolesForUser(String userId) {
        Set<Role> roles = new HashSet<>();
        List<KeycloakRoleResponse> keycloakRoles = keycloakRepository.getUserRoles(userId);

        keycloakRoles.forEach(role -> {
            if (Objects.equals(role.getName(), Role.EMPLOYEE.name())) {
                roles.add(Role.EMPLOYEE);
            }
            if (Objects.equals(role.getName(), Role.CLIENT.name())) {
                roles.add(Role.CLIENT);
            }
        });

        return roles;
    }
}
