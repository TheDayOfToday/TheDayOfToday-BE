package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
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

    public Long join(User user) {


    User vaildUser = userRepository.findByEmail(user.getEmail());
        if(vaildUser!=null) {
        throw new IllegalArgumentException("해당 email은 이미 존재합니다");
    }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 값입니다.");
        }
        User newUser = User.builder()
                .nickname(user.getNickname())
                .name(user.getName())
                .email(user.getEmail())
                .password(encoder.encode(user.getPassword()))
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .diaries(user.getDiaries())
                .notices(user.getNotices())
                .weeklyDataList(user.getWeeklyDataList())
                .build();

        userRepository.save(newUser);

        return newUser.getUserId();
    }

}
