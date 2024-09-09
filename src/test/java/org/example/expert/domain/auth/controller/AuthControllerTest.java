package org.example.expert.domain.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.controller.TodoController;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(controllers = {AuthController.class})
class AuthControllerTest {

    @MockBean
    private AuthService authService;

    @Mock
    private AuthUserArgumentResolver resolver;

    @Autowired
    private AuthController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(resolver)
            .build();
    }

    @Test
    public void 회원가입() throws Exception {
        // given
        SignupRequest request = new SignupRequest("email@com", "password", UserRole.USER.name());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        SignupResponse response = new SignupResponse("token");
        given(authService.signup(any(SignupRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(post("/auth/signup")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.bearerToken").value("token"))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());
    }

    @Test
    public void 로그인() throws Exception {
        // given
        SigninRequest request = new SigninRequest("email@com", "password");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(request);

        SigninResponse response = new SigninResponse("token");
        given(authService.signin(any(SigninRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(post("/auth/signin")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.bearerToken").value("token"))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());
    }
}