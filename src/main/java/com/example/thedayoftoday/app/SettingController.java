package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.login.signup.SignupRequestDto;
import com.example.thedayoftoday.domain.dto.setting.UserSettingDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setting")
public class SettingController {

    private final UserRepository userRepository;
    private final UserService userService;

    public SettingController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/default")
    public UserSettingDto getSetting(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        return new UserSettingDto(user.getName(), user.getEmail(), user.getPhoneNumber());
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestParam String newPassword) {

        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Long> addUser(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        Long id = userService.join(signupRequestDto);
        return ResponseEntity.ok(id);
    }
}
