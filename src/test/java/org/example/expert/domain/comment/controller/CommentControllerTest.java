package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
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

@WebMvcTest(controllers = {CommentController.class})
class CommentControllerTest {

    @MockBean
    private CommentService commentService;

    @Mock
    private AuthUserArgumentResolver resolver;

    @Autowired
    private CommentController controller;

    private MockMvc mockMvc;


    @BeforeEach
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(resolver)
            .build();
    }

    @Test
    public void save() throws Exception {
        // given
        given(resolver.supportsParameter(any())).willReturn(true);
        given(resolver.resolveArgument(any(), any(), any(), any())).willReturn(
            new AuthUser(1L, "email", UserRole.USER));
        CommentSaveRequest saveRequest = new CommentSaveRequest("test");
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(saveRequest);
        CommentSaveResponse resonse = new CommentSaveResponse(1L, "test",
            new UserResponse(1L, "email"));

        when(commentService.saveComment(any(AuthUser.class), anyLong(),
            any(CommentSaveRequest.class))).thenReturn(
            new CommentSaveResponse(1L, "test", new UserResponse(1L, "email")));

        // when / then
        mockMvc.perform(post("/todos/{todoId}/comments", 1)
                .content(s)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.contents").value("test"));
    }

    @Test
    public void getComments() throws Exception {
        // given
        CommentResponse r1 = new CommentResponse(1L, "test1", new UserResponse(1L, "email"));
        CommentResponse r2 = new CommentResponse(2L, "test2", new UserResponse(2L, "email2"));
        CommentResponse r3 = new CommentResponse(3L, "test3", new UserResponse(3L, "email3"));
        List<CommentResponse> l = List.of(r1, r2, r3);
        long todoId = 1L;
        given(commentService.getComments(todoId)).willReturn(l);

        // when
        mockMvc.perform(get("/todos/{todoId}/comments", 1)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].contents").value("test1"))
            .andExpect(jsonPath("$[0].user.id").value(1L))
            .andExpect(jsonPath("$[0].user.email").value("email"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].contents").value("test2"))
            .andExpect(jsonPath("$[1].user.id").value(2L))
            .andExpect(jsonPath("$[1].user.email").value("email2"));
        // then
    }

}