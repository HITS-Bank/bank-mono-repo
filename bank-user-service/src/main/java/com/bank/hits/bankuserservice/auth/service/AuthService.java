package com.bank.hits.bankuserservice.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.bank.hits.bankuserservice.auth.dto.LoginRequest;
import com.bank.hits.bankuserservice.auth.dto.RefreshTokenRequest;
import com.bank.hits.bankuserservice.auth.dto.RegisterRequest;
import com.bank.hits.bankuserservice.auth.dto.TokenResponse;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.exception.*;
import com.bank.hits.bankuserservice.common.exception.message.UserServiceExceptionMessage;
import com.bank.hits.bankuserservice.common.mapper.UserMapper;
import com.bank.hits.bankuserservice.common.model.Channel;
import com.bank.hits.bankuserservice.common.model.UserEntity;
import com.bank.hits.bankuserservice.common.repository.UserRepository;
import com.bank.hits.bankuserservice.common.util.JwtUtils;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    public TokenResponse login(LoginRequest request, Channel channel) {
        UserEntity user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException(UserServiceExceptionMessage.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException(UserServiceExceptionMessage.INVALID_CREDENTIALS);
        }

        if (!isAllowedChannel(channel, user.getRole())) {
            throw new ForbiddenActionException(UserServiceExceptionMessage.FORBIDDEN_ACTION);
        }

        return getTokenResponse(user);
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        if (!jwtUtils.isTokenValid(request.refreshToken())) {
            throw new UnauthorizedException(UserServiceExceptionMessage.INVALID_REFRESH_TOKEN);
        }

        UUID userId = jwtUtils.extractUserId(request.refreshToken());
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UnauthorizedException(UserServiceExceptionMessage.INVALID_REFRESH_TOKEN));

        return getTokenResponse(user);
    }

    @Transactional
    public void register(RegisterRequest request, UUID employeeId) {
        Optional<UserEntity> initiator = userRepository.findById(employeeId);
        if (initiator.isEmpty()) {
            throw new InitiatorUserNotFoundException(UserServiceExceptionMessage.INITIATOR_NOT_FOUND);
        }

        if (initiator.get().getIsBanned()) {
            throw new ForbiddenActionException(UserServiceExceptionMessage.INITIATOR_BANNED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(UserServiceExceptionMessage.USER_ALREADY_EXISTS);
        }

        UserEntity newUser = new UserEntity();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setIsBanned(false);
        newUser.setRole(request.getRole());

        userRepository.save(newUser);
    }

    private TokenResponse getTokenResponse(UserEntity user) {
        UserDto userDto = userMapper.toDto(user);
        String accessToken = jwtUtils.generateAccessToken(userDto);
        String refreshToken = jwtUtils.generateRefreshToken(userDto);
        String accessTokenExpiresAt = getTokenExpiresAt(accessToken);
        String refreshTokenExpiresAt = getTokenExpiresAt(refreshToken);

        return new TokenResponse(accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt);
    }

    private Boolean isAllowedChannel(Channel channel, UserEntity.Role role) {
        return (channel.equals(Channel.EMPLOYEE) && role.equals(UserEntity.Role.EMPLOYEE)) ||
                (channel.equals(Channel.CLIENT) && role.equals(UserEntity.Role.CLIENT));
    }

    private String getTokenExpiresAt(String token) {
        return jwtUtils
                .extractExpirationMillis(token)
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
