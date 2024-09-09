package org.example.expert.domain.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import java.util.List;
import java.util.Optional;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    public void 일정_작성자는_본인을_담당자로_등록할_수_없다() throws Exception {
        // given
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(1L);

        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given((userRepository.findById(managerSaveRequest.getManagerUserId()))).willReturn(Optional.of(user));
        // when
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> managerService.saveManager(authUser, todoId, managerSaveRequest));
        assertEquals(ex.getMessage(), "일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
        // then
    }
    @Test
    public void manager_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void 매니저_저장할_때_일정_담당자가_유저아이디랑_다를_때() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        AuthUser authUser2 = new AuthUser(2L, "a@a.com", UserRole.USER);
        User user2 = User.fromAuthUser(authUser2);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user2);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when / then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class,
            () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

        assertEquals(ex.getMessage(), "담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    public void 매니저_삭제_시_유저가_없을_때() throws Exception {
        // given
        long id = 1L;
        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> managerService.deleteManager(id, 0, 0));
        assertEquals(ex.getMessage(), "User not found");
    }

    @Test
    public void 매니저_삭제_시_일정이_없을_때() throws Exception {
        // given
        long id = 1L;
        User user = new User("d","d", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        given(todoRepository.findById(id)).willReturn(Optional.empty());

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> managerService.deleteManager(id, id, 0));
        assertEquals(ex.getMessage(), "Todo not found");
    }

    @Test
    public void 매니저_삭제_시_유저아이디와_투두_작성자의_아이디가_다를_때() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        User user2 = new User("b@b.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user2, "id", 33L);

        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        System.out.println(user2.getId());
        System.out.println(todo.getUser());
        System.out.println(todo.getUser().getId());
        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> managerService.deleteManager(user2.getId(), todoId,  0));
        assertEquals(ex.getMessage(), "해당 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    public void 매니저_삭제_시_유저아이디와_투두_작성자의_유효하지_않을_때() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", null);

        User user2 = new User("b@b.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user2, "id", 33L);

        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> managerService.deleteManager(user2.getId(), todoId,  0));
        assertEquals(ex.getMessage(), "해당 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    public void 매니저_삭제_시_매니저가_없을때() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerId = 1L;
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(user.getId())).willReturn(Optional.empty());

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> managerService.deleteManager(user.getId(), todoId,  user.getId()));
        assertEquals(ex.getMessage(), "Manager not found");
    }

    @Test
    public void 매니저_삭제_시_해당_일정에_등록된_아이디가_아닐때() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long userId2 = 2L;
        User user2 = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(user2, "id", userId2);

        long todoId2 = 2L;
        Todo todo2 = new Todo("Test Title", "Test Contents", "Sunny", user2);
        ReflectionTestUtils.setField(todo2, "id", todoId2);

        long managerId = 1L;
        Manager manager = new Manager(user2, todo2);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(userId2)).willReturn(Optional.of(manager));

        // when/then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> managerService.deleteManager(user.getId(), todoId,  userId2));
        assertEquals(ex.getMessage(), "해당 일정에 등록된 담당자가 아닙니다.");
    }

    @Test
    public void 매니저_삭제_성공() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long userId2 = 2L;
        User user2 = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(user2, "id", userId2);

        long managerId = 2L;
        Manager manager = new Manager(user2, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(userId2)).willReturn(Optional.of(manager));
        doNothing().when(managerRepository).delete(manager);

        managerService.deleteManager(user.getId(), todoId, userId2);

        Mockito.verify(managerRepository, Mockito.times(1)).delete(manager);
    }

}
