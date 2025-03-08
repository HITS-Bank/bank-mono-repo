package ru.hitsbank.user_service.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hitsbank.user_service.common.dto.UserDto;
import ru.hitsbank.user_service.common.mapper.UserMapper;
import ru.hitsbank.user_service.common.model.UserEntity;
import ru.hitsbank.user_service.common.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto getUserById(UUID userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        return userEntity.map(userMapper::toDto).orElse(null);
    }

    @Transactional
    @PostConstruct
    public void insertAdmin() {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword("$2a$10$Oi7w/OGXE2BFXUlhj94q/.61/L1SuVjk1spkGBo6WxNQIOAC.VNt6");
            admin.setIsBanned(false);
            admin.setRole(UserEntity.Role.EMPLOYEE);
            userRepository.save(admin);
        }
    }
}
