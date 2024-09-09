package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

import java.util.Optional;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // 유저 가져오기
    @Test
    public void 유저조회_실패() throws Exception {
        // given
        long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> userService.getUser(userId));
        assertEquals(ex.getMessage(), "User not found");
    }

    @Test
    public void 유저조회_성공() throws Exception {
        // given
        long userId = 1L;
        User user = new User("aa","aaa", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse response  = userService.getUser(userId);

        // then
        assertEquals(response.getId(), user.getId());
        assertEquals(response.getEmail(), user.getEmail());
    }
    
    // 비밀번호 변경
    @Test
    public void 비밀번호_변경_시_유효성_검사_실패() throws Exception {
        // given
        long userId = 1L;
        User user = new User("aa","aaa", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        String newPassword = "newPassword";

        UserChangePasswordRequest req = new UserChangePasswordRequest("aaa", newPassword);

        // when
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> userService.changePassword(1L, req));
        // then
        assertEquals(ex.getMessage(), "새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
    }

    @Test
    public void 비밀번호_변경_시_유저_못찾았을_때() throws Exception {
        // given
        long userId = 1L;
        User user = new User("aa","aaa", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        String newPassword = "1asdfADSA!@#a";

        UserChangePasswordRequest req = new UserChangePasswordRequest("aaa", newPassword);
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        // when / then
        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, req));
        assertEquals(ex.getMessage(), "User not found");
    }

    @Test
    public void 새_비밀번호가_기존_비밀번호와_같을_때() throws Exception {
        // given
        long userId = 1L;
        User user = new User("aa","1asdfADSA!@#a", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        String newPassword = "1asdfADSA!@#a";

        UserChangePasswordRequest req = new UserChangePasswordRequest("aaa", newPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("1asdfADSA!@#a", newPassword)).willReturn(true);

        // when / then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> userService.changePassword(1L, req));
        assertEquals(ex.getMessage(), "새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }

    @Test
    public void 기존_비밀번호와_유저의_비밀번호가_다를_때() throws Exception {
        // given
        long userId = 1L;
        User user = new User("aa","1asdfADSA!@#", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        String newPassword = "1asdfADSA!@#a";

        UserChangePasswordRequest req = new UserChangePasswordRequest("1asdfADSA!@#aa", newPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        lenient().when(passwordEncoder.matches(anyString(), eq(user.getPassword()))).thenReturn(false);
        lenient().when(passwordEncoder.matches(anyString(), eq(user.getPassword()))).thenReturn(false);
        // when / then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> userService.changePassword(1L, req));
        assertEquals(ex.getMessage(), "잘못된 비밀번호입니다.");
    }

    @Test
    public void 비밀번호_변경_성공() throws Exception {
        // given
        long userId = 1L;
        User user = new User("aa","1asdfADSA!@#", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        String newPassword = "1asdfADSA!@#213a";

        UserChangePasswordRequest req = new UserChangePasswordRequest("1asdfADSA!@#", newPassword);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        lenient().when(passwordEncoder.matches(eq(req.getNewPassword()), eq(user.getPassword()))).thenReturn(false);
        lenient().when(passwordEncoder.matches(eq(req.getOldPassword()), eq(user.getPassword()))).thenReturn(true);
        given(passwordEncoder.encode(req.getNewPassword())).willReturn(newPassword);
        // when / then
        userService.changePassword(1L, req);

        assertEquals(user.getPassword(), newPassword);
    }

}