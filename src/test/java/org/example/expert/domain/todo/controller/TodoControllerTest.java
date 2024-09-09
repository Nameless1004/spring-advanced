package org.example.expert.domain.todo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(controllers = {TodoController.class})
class TodoControllerTest {

    @MockBean
    private TodoService todoService;

    @Mock
    private AuthUserArgumentResolver resolver;

    @Autowired
    private TodoController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(resolver)
            .build();
    }

    @Test
    public void 일정_저장() throws Exception {
        // given
        given(resolver.supportsParameter(any())).willReturn(true);
        given(resolver.resolveArgument(any(), any(), any(), any())).willReturn(
            new AuthUser(1L, "email", UserRole.USER));

        TodoSaveRequest tsr = new TodoSaveRequest("title", "contents");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(tsr);

        TodoSaveResponse tsrp = new TodoSaveResponse(1L, "title", "contents", "weather",
            new UserResponse(1L, "email"));
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(tsrp);

        // when / then
        mockMvc.perform(post("/todos")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("title"))
            .andExpect(jsonPath("$.contents").value("contents"))
            .andExpect(jsonPath("$.weather").value("weather"))
            .andExpect(jsonPath("$.user.id").value(1L))
            .andExpect(jsonPath("$.user.email").value("email"))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());
    }

    @Test
    public void 일정_단건_조회() throws Exception {
        // given
        long todoId = 1L;
        TodoResponse res = new TodoResponse(1L, "title", "contents", "weather",
            new UserResponse(1L, "email"), LocalDateTime.now(), LocalDateTime.now());
        given(todoService.getTodo(todoId)).willReturn(res);

        // when / then
        mockMvc.perform(get("/todos/{todoId}", todoId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("title"))
            .andExpect(jsonPath("$.contents").value("contents"))
            .andExpect(jsonPath("$.weather").value("weather"))
            .andExpect(jsonPath("$.user.id").value(1L))
            .andExpect(jsonPath("$.user.email").value("email"))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());

    }

    @Test
    public void 일정다건조회() throws Exception {
        // given
        given(resolver.supportsParameter(any())).willReturn(true);
        given(resolver.resolveArgument(any(), any(), any(), any())).willReturn(
            new AuthUser(1L, "email", UserRole.USER));
        User u1 = new User("email1","password", UserRole.USER);
        User u2 = new User("email1","password", UserRole.USER);
        User u3 = new User("email1","password", UserRole.USER);
        ReflectionTestUtils.setField(u1, "id", 1L);
        ReflectionTestUtils.setField(u2, "id", 2L);
        ReflectionTestUtils.setField(u3, "id", 3L);


        LocalDateTime n = LocalDateTime.now();
        TodoResponse tr1 = new TodoResponse(1L, "test1", "testContents1", "weather1",new UserResponse(u1.getId(), u1.getEmail()), n, n);
        TodoResponse tr2 = new TodoResponse(2L, "test2", "testContents2", "weather2",new UserResponse(u2.getId(), u2.getEmail()), n, n);
        TodoResponse tr3 = new TodoResponse(3L, "test3", "testContents3", "weather3",new UserResponse(u3.getId(), u3.getEmail()), n, n);

        Pageable pageable = PageRequest.of(1, 10);
        Page<TodoResponse> result = new PageImpl<>(List.of(tr1, tr2, tr3), pageable,1);
        given(todoService.getTodos(anyInt(), anyInt())).willReturn(result);

        // when / then
        mockMvc.perform(get("/todos")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content.size()").value(3))
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].title").value("test1"))
            .andExpect(jsonPath("$.content[0].contents").value("testContents1"))
            .andExpect(jsonPath("$.content[0].weather").value("weather1"))
            .andExpect(jsonPath("$.content[0].user.id").value(u1.getId()))
            .andExpect(jsonPath("$.content[0].user.email").value(u1.getEmail()))
            .andExpect(jsonPath("$.content[1].id").value(2L))
            .andExpect(jsonPath("$.content[1].title").value("test2"))
            .andExpect(jsonPath("$.content[1].contents").value("testContents2"))
            .andExpect(jsonPath("$.content[1].weather").value("weather2"))
            .andExpect(jsonPath("$.content[1].user.id").value(u2.getId()))
            .andExpect(jsonPath("$.content[1].user.email").value(u2.getEmail()))
            .andExpect(jsonPath("$.content[2].id").value(3L))
            .andExpect(jsonPath("$.content[2].title").value("test3"))
            .andExpect(jsonPath("$.content[2].contents").value("testContents3"))
            .andExpect(jsonPath("$.content[2].weather").value("weather3"))
            .andExpect(jsonPath("$.content[2].user.id").value(u3.getId()))
            .andExpect(jsonPath("$.content[2].user.email").value(u3.getEmail()))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andDo(print());
    }
}