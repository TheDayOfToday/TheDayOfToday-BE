package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.login.SignupRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import com.example.thedayoftoday.domain.repository.UserRepository;
import jakarta.validation.Valid;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public Long join(@Valid SignupRequestDto user) {
        boolean validUser = userRepository.existsByEmail(user.email());

        if (validUser) {
            throw new IllegalArgumentException("해당 email은 이미 존재합니다");
        }

        User newUser = User.builder()
                .name(user.name())
                .email(user.email())
                .password(encoder.encode(user.password()))
                .phoneNumber(user.phoneNumber())
                .role(RoleType.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        return savedUser.getUserId();
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
