package org.example.expert.domain.todo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    // save
    @Test
    public void 일정저장() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        TodoSaveRequest request = new TodoSaveRequest("title", "contents");
        Todo todo = new Todo(request.getTitle(), request.getContents(), "test", user);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        when(weatherClient.getTodayWeather()).thenReturn("test");
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, request);

        // then
        assertEquals(todoSaveResponse.getTitle(), todo.getTitle());
        assertEquals(todoSaveResponse.getContents(), todo.getContents());
        assertEquals(todoSaveResponse.getWeather(), "test");
        assertEquals(todoSaveResponse.getUser().getId(), user.getId());
        assertEquals(todoSaveResponse.getUser().getEmail(), user.getEmail());
    }

    // gets
    @Test
    public void 일정목록조회() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        TodoSaveRequest request = new TodoSaveRequest("title", "contents");
        Todo todo = new Todo(request.getTitle(), request.getContents(), "test", user);

        ArrayList<Todo> todos = new ArrayList<>();
        todos.add(todo);
        todos.add(todo);
        todos.add(todo);
        todos.add(todo);
        todos.add(todo);

        Page<Todo> page =  new PageImpl<Todo>(todos);
        given(todoRepository.findAllByOrderByModifiedAtDesc(any())).willReturn(page);

        // when
        Page<TodoResponse> todos1 = todoService.getTodos(1, 5);

        // then
        assertEquals(todos1.getTotalElements(), 5);
    }

    // get
    @Test
    public void 일정단건조회_일정_찾을_수_없을_때() throws Exception {
        // given
        long todoId = 1L;
        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());
        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> todoService.getTodo(todoId));
        assertEquals(ex.getMessage(), "Todo not found");
    }

    @Test
    public void 일정단건조회() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        TodoSaveRequest request = new TodoSaveRequest("title", "contents");
        Todo todo = new Todo(request.getTitle(), request.getContents(), "test", user);
        long todoId = 1L;
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when
        TodoResponse res = todoService.getTodo(todoId);

        // then
        assertEquals(res.getId(), todo.getId());
        assertEquals(res.getTitle(), todo.getTitle());
        assertEquals(res.getContents(), todo.getContents());
        assertEquals(res.getUser().getId(), todo.getUser().getId());
        assertEquals(res.getUser().getEmail(), todo.getUser().getEmail());
    }
}