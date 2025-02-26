package com.example.thedayoftoday.domain.security.service;

import com.example.thedayoftoday.domain.dto.CustomUserInfoDto;
import com.example.thedayoftoday.domain.dto.LoginRequestDto;
import com.example.thedayoftoday.domain.dto.SignupRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import com.example.thedayoftoday.domain.repository.UserRepository;

import com.example.thedayoftoday.domain.security.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void authProcess(SignupRequestDto signupDTO) {

        if(userRepository.existsByEmail(signupDTO.getEmail())) {
            throw new UsernameNotFoundException("이미 존재하는 이메일입니다");
        }
        User newUser = User.builder()
                .nickname(signupDTO.getNickname())
                .name(signupDTO.getName())
                .email(signupDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(signupDTO.getPassword()))  // ✅ 비밀번호 암호화
                .phoneNumber(signupDTO.getPhoneNumber())
                .role(RoleType.USER)
                .build();

        // DB에 저장
        userRepository.save(newUser);

    }
}
