package thedayoftoday.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.notice.entity.Notice;
import thedayoftoday.domain.weeklyData.entity.WeeklyData;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class UserTest {

    @Test
    @DisplayName("createUser: 정적 팩토리로 USER 생성 및 비밀번호 인코딩")
    void createUser_success() {
        // given
        SignupRequestDto req = new SignupRequestDto(
                "홍길동", "test@example.com", "rawPwd!", "010-1234-5678"
        );
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        given(encoder.encode("rawPwd!")).willReturn("ENC_rawPwd!");

        // when
        User user = User.createUser(req, encoder);

        // then
        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("ENC_rawPwd!");
        assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(user.getRole()).isEqualTo(RoleType.USER);
    }

    @Test
    @DisplayName("updatePassword: 새 비밀번호로 성공적으로 변경")
    void updatePassword_success() {
        // given
        User user = User.builder()
                .name("홍길동")
                .email("test@example.com")
                .password("ENC_OLD")
                .phoneNumber("010-0000-0000")
                .role(RoleType.USER)
                .build();

        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        given(encoder.matches("newRaw", "ENC_OLD")).willReturn(false);
        given(encoder.encode("newRaw")).willReturn("ENC_NEW");

        // when
        user.updatePassword("newRaw", encoder);

        // then
        assertThat(user.getPassword()).isEqualTo("ENC_NEW");
    }

    @Test
    @DisplayName("updatePassword: 기존 비밀번호와 동일하면 예외")
    void updatePassword_fail_samePassword() {
        // given
        User user = User.builder()
                .name("홍길동")
                .email("test@example.com")
                .password("ENC_SAME")
                .phoneNumber("010-0000-0000")
                .role(RoleType.USER)
                .build();

        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        given(encoder.matches("sameRaw", "ENC_SAME")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> user.updatePassword("sameRaw", encoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기존의 비밀번호와 동일");
    }

    @Test
    @DisplayName("addDiary: 컬렉션 추가 + 양방향 세팅")
    void addDiary_setsBothSides() {
        // given
        User user = User.builder()
                .name("홍길동").email("u@e.com").password("p").role(RoleType.USER)
                .build();
        Diary diary = Diary.builder().build();

        // when
        user.addDiary(diary);

        // then
        assertThat(user.getDiaries()).contains(diary);
        assertThat(diary.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("addNotice: 컬렉션 추가 + 양방향 세팅")
    void addNotice_setsBothSides() {
        // given
        User user = User.builder()
                .name("홍길동").email("u@e.com").password("p").role(RoleType.USER)
                .build();
        Notice notice = Notice.builder().build();

        // when
        user.addNotice(notice);

        // then
        assertThat(user.getNotices()).contains(notice);
        // 엔티티 수정 권장: notice.setUser(this) 호출
        assertThat(notice.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("addWeeklyData: 컬렉션 추가 + 양방향 세팅")
    void addWeeklyData_setsBothSides() {
        // given
        User user = User.builder()
                .name("홍길동").email("u@e.com").password("p").role(RoleType.USER)
                .build();
        WeeklyData weekly = WeeklyData.builder().build();

        // when
        user.addWeeklyData(weekly);

        // then
        assertThat(user.getWeeklyDataList()).contains(weekly);
        // 엔티티 수정 권장: weeklyData.setUser(this) 호출
        assertThat(weekly.getUser()).isEqualTo(user);
    }
}
