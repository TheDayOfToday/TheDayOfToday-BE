package thedayoftoday.domain.user.dto;

public record ResetPasswordRequestDto(String email, String newPassword) {
}
