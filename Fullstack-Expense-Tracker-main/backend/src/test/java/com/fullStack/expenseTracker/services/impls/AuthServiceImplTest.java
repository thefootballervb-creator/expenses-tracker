package com.fullStack.expenseTracker.services.impls;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.enums.ERole;
import com.fullStack.expenseTracker.exceptions.UserAlreadyExistsException;
import com.fullStack.expenseTracker.factories.RoleFactory;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.UserRepository;
import com.fullStack.expenseTracker.services.UserService;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleFactory roleFactory;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void save_whenUsernameExists_shouldThrowException() {
        SignUpRequestDto request = new SignUpRequestDto("jane", "jane@example.com", "password123");

        when(userService.existsByUsername("jane")).thenReturn(true);

        UserAlreadyExistsException exception =
                assertThrows(UserAlreadyExistsException.class, () -> authService.save(request));
        assertEquals("Registration Failed: username is already taken!", exception.getMessage());

        verifyNoInteractions(userRepository);
    }

    @Test
    @SuppressWarnings("null")
    void save_whenNewUser_shouldPersistEnabledUser() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto("jane", "jane@example.com", "password123");
        Role userRole = new Role(1, ERole.ROLE_USER);

        when(userService.existsByUsername("jane")).thenReturn(false);
        when(userService.existsByEmail("jane@example.com")).thenReturn(false);
        when(roleFactory.getInstance("user")).thenReturn(userRole);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        AtomicReference<User> savedUserRef = new AtomicReference<>();
        //noinspection DataFlowIssue
        org.mockito.Mockito.doAnswer(invocation -> {
            User candidate = invocation.getArgument(0);
            savedUserRef.set(candidate);
            return candidate;
        }).when(userRepository).save(org.mockito.ArgumentMatchers.any());

        ResponseEntity<ApiResponseDto<?>> response = authService.save(request);

        User persistedUser = Objects.requireNonNull(savedUserRef.get(), "User should be persisted");
        assertEquals("jane", persistedUser.getUsername());
        assertEquals("jane@example.com", persistedUser.getEmail());
        assertEquals("encodedPassword", persistedUser.getPassword());
        assertTrue(persistedUser.isEnabled(), "User should be enabled immediately");
        assertEquals(Collections.singleton(userRole), persistedUser.getRoles());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseStatus.SUCCESS, body.getStatus());
        assertEquals("User has been successfully registered!", body.getResponse());
    }

    @Test
    void resetPassword_whenCurrentPasswordMismatch_shouldReturnBadRequest() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setEmail("jane@example.com");
        request.setCurrentPassword("wrongCurrent");
        request.setNewPassword("NewPassword1");

        User user = User.builder()
                .id(1L)
                .username("jane")
                .email("jane@example.com")
                .password("storedPassword")
                .enabled(true)
                .roles(Set.of(new Role(1, ERole.ROLE_USER)))
                .build();

        when(userService.existsByEmail("jane@example.com")).thenReturn(true);
        when(userService.findByEmail("jane@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongCurrent", "storedPassword")).thenReturn(false);

        ResponseEntity<ApiResponseDto<?>> response = authService.resetPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseStatus.FAILED, body.getStatus());
        assertEquals("Reset password not successful: current password is incorrect!!", body.getResponse());

        verifyNoInteractions(userRepository);
    }

    @Test
    @SuppressWarnings("null")
    void resetPassword_whenValidNewPassword_shouldUpdateEncodedPassword() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setEmail("jane@example.com");
        request.setCurrentPassword("");
        request.setNewPassword("NewPassword1");

        User user = User.builder()
                .id(1L)
                .username("jane")
                .email("jane@example.com")
                .password("oldPassword")
                .enabled(true)
                .roles(Set.of(new Role(1, ERole.ROLE_USER)))
                .build();

        when(userService.existsByEmail("jane@example.com")).thenReturn(true);
        when(userService.findByEmail("jane@example.com")).thenReturn(user);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("encodedNewPassword");

        AtomicReference<User> savedUserRef = new AtomicReference<>();
        //noinspection DataFlowIssue
        org.mockito.Mockito.doAnswer(invocation -> {
            User candidate = invocation.getArgument(0);
            savedUserRef.set(candidate);
            return candidate;
        }).when(userRepository).save(org.mockito.ArgumentMatchers.any());

        ResponseEntity<ApiResponseDto<?>> response = authService.resetPassword(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponseDto<?> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseStatus.SUCCESS, body.getStatus());
        assertEquals("Reset successful: Password has been successfully reset!", body.getResponse());
        assertEquals("encodedNewPassword", user.getPassword());

        User savedUser = Objects.requireNonNull(savedUserRef.get(), "User should be saved");
        assertEquals(user, savedUser);
    }
}

