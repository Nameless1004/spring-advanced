package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

import java.util.Optional;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthService authService;

    @Test
    public void 회원가입_이미_존재하는_이메일_일_때(){
        // given
        SignupRequest request = new SignupRequest("aaa@gmail.com", "1234","USER");
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> {
                authService.signup(request);
            });
        assertEquals("이미 존재하는 이메일입니다.", ex.getMessage());
    }

    @Test
    public void 회원가입_성공(){
        // given
        User user = new User("aaa@gmail.com", "1234",UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        SignupRequest request = new SignupRequest("aaa@gmail.com", "1234","USER");
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(passwordEncoder.encode(anyString())).willReturn("1234");
        given(jwtUtil.createToken(anyLong(), anyString(), any(UserRole.class))).willReturn("Bearer 1234");

        // when
        SignupResponse signup = authService.signup(request);

        // then
        assertEquals("Bearer 1234", signup.getBearerToken());
    }

    @Test
    public void 로그인_가입되어_있지_않을_때(){
        // given
        SigninRequest request = new SigninRequest("aaa@mail.com", "1234");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> authService.signin(request));

        assertEquals(ex.getMessage(), "가입되지 않은 유저입니다.");
    }

    @Test
    public void 로그인_시_비밀번호가_일치하지_않을_경우() throws Exception {
        // given
        SigninRequest request = new SigninRequest("aaa@mail.com", "1234");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(new User()));
        lenient().when(passwordEncoder.matches("1", "12")).thenReturn(false);

        // when/then
        AuthException ex = assertThrows(AuthException.class,
            () -> authService.signin(request));

        assertEquals(ex.getMessage(), "잘못된 비밀번호입니다.");
    }

    @Test
    public void 로그인_성공() throws Exception {
        // given
        User user = new User("aaa@gmail.com", "1234",UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        SigninRequest request = new SigninRequest("aaa@mail.com", "1234");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn(user.getId() + user.getEmail() + user.getUserRole());
        given(passwordEncoder.matches(user.getPassword(), user.getPassword())).willReturn(true);

        // when
        SigninResponse signin = authService.signin(request);

        // then
        assertEquals(signin.getBearerToken(), user.getId() + user.getEmail() + user.getUserRole());
    }
}