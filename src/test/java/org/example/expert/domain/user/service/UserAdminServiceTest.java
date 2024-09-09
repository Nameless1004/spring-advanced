package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    public void 유저못찾았을_때() throws Exception {
        // given
        long userId = 1L;
        UserRoleChangeRequest role = new UserRoleChangeRequest("USER");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when / then
        InvalidRequestException ex = assertThrows(
            InvalidRequestException.class, () -> userAdminService.changeUserRole(userId, role));
        assertEquals(ex.getMessage(), "User not found");
    }

    @Test
    public void 역할변경성공() throws Exception {
        // given
        long userId = 1L;
        User user = new User("d", "dd", UserRole.USER);
        UserRoleChangeRequest role = new UserRoleChangeRequest("ADMIN");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, role);

        // then
        assertEquals(user.getUserRole(), UserRole.ADMIN);
    }
}