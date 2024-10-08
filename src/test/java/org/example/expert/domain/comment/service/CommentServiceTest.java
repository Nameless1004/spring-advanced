package org.example.expert.domain.comment.service;

import java.util.List;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void 할일에_해당하는_담당자가_아니면_댓글을_달_수_없다() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        AuthUser authUser2 = new AuthUser(2L, "email", UserRole.USER);
        CommentSaveRequest request = new CommentSaveRequest("contents");
        User user = User.fromAuthUser(authUser);
        User user2 = User.fromAuthUser(authUser2);
        Todo todo = new Todo("title", "contents", "weather", user);

        // when
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> {
                commentService.saveComment(authUser2, 1L, request);
            });

        // then
        assertEquals(ex.getMessage(), "Todo user id mismatch");
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    public void 코멘트_가져오기() throws Exception {
        // given
        Long todoId = 1L;
        Comment comment = new Comment();
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        ReflectionTestUtils.setField(comment,"id",1L);
        ReflectionTestUtils.setField(comment,"contents","comment");
        ReflectionTestUtils.setField(comment,"user",user);

        List<Comment> commentList = List.of(comment);
        given(commentRepository.findByTodoIdWithUser(todoId )).willReturn(commentList);

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertEquals(1, result.size());
        assertEquals("comment", result.get(0).getContents());
        assertEquals(1L, result.get(0).getUser().getId());
        assertEquals("email", result.get(0).getUser().getEmail());
    }
}
