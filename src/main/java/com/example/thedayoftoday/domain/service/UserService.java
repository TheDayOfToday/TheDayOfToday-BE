package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.SignupRequestDto;
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

    public Long join(@Valid  SignupRequestDto user) {
        User vaildUser = userRepository.findByEmail(user.getEmail());
        if (vaildUser != null) {
            throw new IllegalArgumentException("해당 email은 이미 존재합니다");
        }

        User newUser = User.builder()
                .nickname(user.getNickname())
                .name(user.getName())
                .email(user.getEmail())
                .password(encoder.encode(user.getPassword()))
                .phoneNumber(user.getPhoneNumber())
                .role(RoleType.USER)
                .build();

        userRepository.save(newUser);
        return newUser.getUserId();
    }


}
