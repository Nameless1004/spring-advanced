package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentAdminService;
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

@WebMvcTest(controllers = {CommentAdminController.class})
class CommentAdminControllerTest {

    @MockBean
    private CommentAdminService commentAdminService;

    @Mock
    private AuthUserArgumentResolver resolver;

    @Autowired
    private CommentAdminController controller;

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
        doNothing().when(commentAdminService).deleteComment(anyLong());
        // when / then
        mockMvc.perform(delete("/admin/comments/{commentId}", 1)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()));

        verify(commentAdminService, times(1)).deleteComment(anyLong());
    }

}