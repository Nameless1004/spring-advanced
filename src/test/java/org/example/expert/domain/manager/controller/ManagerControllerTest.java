package org.example.expert.domain.manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(controllers = {ManagerController.class})
class ManagerControllerTest {

    @MockBean
    private ManagerService managerService;

    @MockBean
    private JwtUtil jwtUtil;

    @Mock
    private AuthUserArgumentResolver resolver;

    @Autowired
    private ManagerController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(resolver)
            .build();
    }

    @Test
    public void 삭제() throws Exception {
        // given
        given(resolver.supportsParameter(any())).willReturn(true);
        given(resolver.resolveArgument(any(), any(), any(), any())).willReturn(new AuthUser(1L, "email", UserRole.USER));
        long userId = 1L;
        long todoId = 1L;
        long managerId = 1L;
        doNothing().when(managerService).deleteManager(userId,todoId, managerId);
        // when / then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()));

        verify(managerService, times(1)).deleteManager(userId,todoId, managerId);
    }

    @Test
    public void 저장() throws Exception {
        // given
        long todoId = 1L;
        ManagerSaveResponse msrp = new ManagerSaveResponse(1L, new UserResponse(1L, "email"));
        ManagerSaveRequest msrq = new ManagerSaveRequest(1L);
        given(resolver.supportsParameter(any())).willReturn(true);
        given(resolver.resolveArgument(any(), any(), any(), any())).willReturn(new AuthUser(1L, "email", UserRole.USER));
        given(managerService.saveManager(any(AuthUser.class), anyLong(),
            any(ManagerSaveRequest.class))).willReturn(msrp);
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(msrq);
        // when / then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .content(s)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.user.id").value(1L))
            .andExpect(jsonPath("$.user.email").value("email"))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());

    }

    @Test
    public void 매니저조회() throws Exception {
        // given
        long todoId = 1L;
        ManagerResponse m1 = new ManagerResponse(1L, new UserResponse(1L, "em1"));
        ManagerResponse m2 = new ManagerResponse(2L, new UserResponse(2L, "em2"));
        ManagerResponse m3 = new ManagerResponse(3L, new UserResponse(3L, "em3"));
        List<ManagerResponse> l = List.of(m1, m2, m3);
        given(managerService.getManagers(todoId)).willReturn(l);

        // when / then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId).contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].user.id").value(1L))
            .andExpect(jsonPath("$[0].user.email").value("em1"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].user.id").value(2L))
            .andExpect(jsonPath("$[1].user.email").value("em2"))
            .andExpect(jsonPath("$[2].id").value(3L))
            .andExpect(jsonPath("$[2].user.id").value(3L))
            .andExpect(jsonPath("$[2].user.email").value("em3"))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());
    }
}