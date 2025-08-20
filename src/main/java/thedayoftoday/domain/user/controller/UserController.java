package thedayoftoday.domain.user.controller;

import lombok.RequiredArgsConstructor;
import thedayoftoday.domain.user.service.UserService;
import thedayoftoday.domain.user.dto.PasswordUpdateRequest;
import thedayoftoday.domain.user.dto.ResetPasswordRequestDto;
import thedayoftoday.domain.user.dto.UserInfoDto;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> getSetting(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoDto userInfo = userService.getUserInfo(userDetails.getUserId());
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto requestDto) {
        userService.resetPassword(requestDto);
        return ResponseEntity.ok("비밀번호 변경이 완료되었습니다.");
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok("비밀번호 변경이 완료되었습니다.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getUserId());
        return ResponseEntity.ok("회원 삭제가 완료되었습니다.");
    }
}
