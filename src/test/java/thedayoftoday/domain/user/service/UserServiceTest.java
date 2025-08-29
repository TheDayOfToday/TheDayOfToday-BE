package thedayoftoday.domain.user.service;

import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.auth.exception.PhoneNumberDuplicationExceptiono;
import thedayoftoday.domain.auth.mail.exception.EmailDuplicationException;
import thedayoftoday.domain.user.dto.PasswordUpdateRequest;
import thedayoftoday.domain.user.dto.ResetPasswordRequestDto;
import thedayoftoday.domain.user.dto.UserInfoDto;
import thedayoftoday.domain.user.entity.RoleType;
import thedayoftoday.domain.user.entity.User;
import thedayoftoday.domain.user.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    // -------------------- findById --------------------
    @Test
    @DisplayName("findById: 사용자를 Optional로 반환한다")
    void findById_returnsOptional() {
        // given
        User user = User.builder()
                .name("홍길동").email("a@b.com").password("ENC").phoneNumber("010")
                .role(RoleType.USER).build();
        ReflectionTestUtils.setField(user, "userId", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        Optional<User> found = userService.findById(1L);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    // -------------------- getUserInfo --------------------
    @Test
    @DisplayName("getUserInfo: 존재하면 UserInfoDto로 반환한다")
    void getUserInfo_success() {
        // given
        User user = User.builder()
                .name("홍길동").email("a@b.com").password("ENC").phoneNumber("010-0000-0000")
                .role(RoleType.USER).build();
        ReflectionTestUtils.setField(user, "userId", 7L);
        given(userRepository.findById(7L)).willReturn(Optional.of(user));

        // when
        UserInfoDto dto = userService.getUserInfo(7L);

        // then
        assertThat(dto.userId()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("홍길동");
        assertThat(dto.email()).isEqualTo("a@b.com");
        assertThat(dto.phoneNumber()).isEqualTo("010-0000-0000");
        verify(userRepository).findById(7L);
    }

    @Test
    @DisplayName("getUserInfo: 없으면 IllegalArgumentException")
    void getUserInfo_notFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserInfo(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자");
        verify(userRepository).findById(99L);
    }

    // -------------------- resetPassword (email 기반) --------------------
    @Test
    @DisplayName("resetPassword: 이메일로 찾아 새 비밀번호로 변경한다")
    void resetPassword_success() {
        // given
        User user = User.builder()
                .name("홍길동").email("x@y.com").password("ENC_OLD").phoneNumber("010")
                .role(RoleType.USER).build();

        given(userRepository.findByEmail("x@y.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("newRaw", "ENC_OLD")).willReturn(false);
        given(passwordEncoder.encode("newRaw")).willReturn("ENC_NEW");

        ResetPasswordRequestDto req = new ResetPasswordRequestDto("x@y.com", "newRaw");

        // when
        userService.resetPassword(req);

        // then
        assertThat(user.getPassword()).isEqualTo("ENC_NEW");
        verify(userRepository).findByEmail("x@y.com");
        verify(passwordEncoder).encode("newRaw");
    }

    @Test
    @DisplayName("resetPassword: 이메일이 없으면 IllegalArgumentException")
    void resetPassword_emailNotFound() {
        // given
        given(userRepository.findByEmail("none@none.com")).willReturn(Optional.empty());
        ResetPasswordRequestDto req = new ResetPasswordRequestDto("none@none.com", "newRaw");

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 이메일");
        verify(userRepository).findByEmail("none@none.com");
    }

    @Test
    @DisplayName("resetPassword: 기존과 동일한 새 비밀번호면 IllegalArgumentException")
    void resetPassword_samePassword() {
        // given
        User user = User.builder()
                .name("홍길동").email("x@y.com").password("ENC_SAME").phoneNumber("010")
                .role(RoleType.USER).build();

        given(userRepository.findByEmail("x@y.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("sameRaw", "ENC_SAME")).willReturn(true);

        ResetPasswordRequestDto req = new ResetPasswordRequestDto("x@y.com", "sameRaw");

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기존의 비밀번호와 동일");
        verify(userRepository).findByEmail("x@y.com");
    }

    // -------------------- updatePassword (userId 기반) --------------------
    @Test
    @DisplayName("updatePassword: userId로 찾아 새 비밀번호로 변경한다")
    void updatePassword_success() {
        // given
        User user = User.builder()
                .name("홍길동").email("a@b.com").password("ENC_OLD").phoneNumber("010")
                .role(RoleType.USER).build();
        ReflectionTestUtils.setField(user, "userId", 3L);

        given(userRepository.findById(3L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("newRaw", "ENC_OLD")).willReturn(false);
        given(passwordEncoder.encode("newRaw")).willReturn("ENC_NEW");

        PasswordUpdateRequest req = new PasswordUpdateRequest("newRaw");

        // when
        userService.updatePassword(3L, req);

        // then
        assertThat(user.getPassword()).isEqualTo("ENC_NEW");
        verify(userRepository).findById(3L);
        verify(passwordEncoder).encode("newRaw");
    }

    @Test
    @DisplayName("updatePassword: 존재하지 않으면 IllegalArgumentException")
    void updatePassword_notFound() {
        // given
        given(userRepository.findById(404L)).willReturn(Optional.empty());
        PasswordUpdateRequest req = new PasswordUpdateRequest("newRaw");

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(404L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자");
        verify(userRepository).findById(404L);
    }

    @Test
    @DisplayName("updatePassword: 동일 비밀번호면 IllegalArgumentException")
    void updatePassword_samePassword() {
        // given
        User user = User.builder()
                .name("홍길동").email("a@b.com").password("ENC_SAME").phoneNumber("010")
                .role(RoleType.USER).build();
        ReflectionTestUtils.setField(user, "userId", 3L);

        given(userRepository.findById(3L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("sameRaw", "ENC_SAME")).willReturn(true);

        PasswordUpdateRequest req = new PasswordUpdateRequest("sameRaw");

        // when & then
        assertThatThrownBy(() -> userService.updatePassword(3L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기존의 비밀번호와 동일");
        verify(userRepository).findById(3L);
    }

    // -------------------- createUser (중복 검사: 이메일/전화 중복이면 예외) --------------------
    @Test
    @DisplayName("createUser: 이메일/전화번호 중복 없으면 신규 유저 생성")
    void createUser_success_noDup() {
        // given
        SignupRequestDto req = new SignupRequestDto("홍길동", "new@ex.com", "raw!", "010-1111-2222");

        given(userRepository.existsByEmail("new@ex.com")).willReturn(false);          // 이메일 중복 아님
        given(userRepository.existsByPhoneNumber("010-1111-2222")).willReturn(false); // 전화 중복 아님
        given(passwordEncoder.encode("raw!")).willReturn("ENC_raw!");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "userId", 100L);
            return u;
        });

        // when
        Long userId = userService.createUser(req);

        // then
        assertThat(userId).isEqualTo(100L);
        verify(userRepository).existsByEmail("new@ex.com");
        verify(userRepository).existsByPhoneNumber("010-1111-2222");
        verify(passwordEncoder).encode("raw!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: 이메일 중복이면 EmailDuplicationException")
    void createUser_fail_emailDuplicate() {
        // given
        SignupRequestDto req = new SignupRequestDto("홍길동", "dup@ex.com", "raw!", "010-1111-2222");
        given(userRepository.existsByEmail("dup@ex.com")).willReturn(true); // 이메일 중복

        // when & then
        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(EmailDuplicationException.class)
                .hasMessageContaining("이미 존재");
        verify(userRepository).existsByEmail("dup@ex.com");
        then(userRepository).should(never()).existsByPhoneNumber(anyString());
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("createUser: 전화번호 중복이면 PhoneNumberDuplicationExceptiono")
    void createUser_fail_phoneDuplicate() {
        // given
        SignupRequestDto req = new SignupRequestDto("홍길동", "ok@ex.com", "raw!", "010-9999-9999");
        given(userRepository.existsByEmail("ok@ex.com")).willReturn(false);           // 이메일 통과
        given(userRepository.existsByPhoneNumber("010-9999-9999")).willReturn(true);  // 전화 중복

        // when & then
        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(PhoneNumberDuplicationExceptiono.class)
                .hasMessageContaining("이미 존재");
        verify(userRepository).existsByEmail("ok@ex.com");
        verify(userRepository).existsByPhoneNumber("010-9999-9999");
        then(userRepository).should(never()).save(any());
    }

    // -------------------- deleteUser --------------------
    @Test
    @DisplayName("deleteUser: 존재하면 삭제")
    void deleteUser_success() {
        // given
        User user = User.builder()
                .name("지워").email("del@ex.com").password("ENC").phoneNumber("010")
                .role(RoleType.USER).build();
        ReflectionTestUtils.setField(user, "userId", 77L);

        given(userRepository.findById(77L)).willReturn(Optional.of(user));

        // when
        userService.deleteUser(77L);

        // then
        verify(userRepository).findById(77L);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteUser: 없으면 IllegalArgumentException")
    void deleteUser_notFound() {
        // given
        given(userRepository.findById(88L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(88L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 계정을 찾을 수 없습니다.");
        verify(userRepository).findById(88L);
    }

    // -------------------- checkEmailExists (존재하면 예외, 없으면 통과) --------------------
    @Test
    @DisplayName("checkEmailExists: 존재하면 EmailDuplicationException")
    void checkEmailExists_exist_throws() {
        // given
        given(userRepository.existsByEmail("dup@ex.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.checkEmailExists("dup@ex.com"))
                .isInstanceOf(EmailDuplicationException.class)
                .hasMessageContaining("이미 존재");
        verify(userRepository).existsByEmail("dup@ex.com");
    }

    @Test
    @DisplayName("checkEmailExists: 존재하지 않으면 예외 없이 통과")
    void checkEmailExists_notExist_ok() {
        // given
        given(userRepository.existsByEmail("none@ex.com")).willReturn(false);

        // when & then
        assertThatCode(() -> userService.checkEmailExists("none@ex.com"))
                .doesNotThrowAnyException();
        verify(userRepository).existsByEmail("none@ex.com");
    }
}
