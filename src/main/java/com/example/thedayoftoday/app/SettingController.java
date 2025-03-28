package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.UserSettingDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettingController {

    private final UserRepository userRepository;

    public SettingController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/setting")
    public UserSettingDto getSetting(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        return new UserSettingDto(user.getName(), user.getPassword(), user.getEmail(), user.getPhoneNumber());
    }

    @PutMapping("update-password")
    public ResponseEntity<Void> updatePassword(@RequestParam Long userId, @RequestParam String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
