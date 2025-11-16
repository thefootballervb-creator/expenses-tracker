package com.fullStack.expenseTracker.services.impls;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.dto.requests.SignUpRequestDto;
import com.fullStack.expenseTracker.enums.ApiResponseStatus;
import com.fullStack.expenseTracker.exceptions.RoleNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserAlreadyExistsException;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserServiceLogicException;
import com.fullStack.expenseTracker.factories.RoleFactory;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.UserRepository;
import com.fullStack.expenseTracker.services.AuthService;
import com.fullStack.expenseTracker.services.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleFactory roleFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<ApiResponseDto<?>> save(SignUpRequestDto signUpRequestDto)
            throws UserAlreadyExistsException, UserServiceLogicException {
        if (userService.existsByUsername(signUpRequestDto.getUserName())) {
            throw new UserAlreadyExistsException("Registration Failed: username is already taken!");
        }
        if (userService.existsByEmail(signUpRequestDto.getEmail())) {
            throw new UserAlreadyExistsException("Registration Failed: email is already taken!");
        }

        try {
            User user = Objects.requireNonNull(
                    createUser(signUpRequestDto),
                    "User creation returned null"
            );

            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
                    ApiResponseStatus.SUCCESS, HttpStatus.CREATED,"User has been successfully registered!"
            ));

        } catch (RoleNotFoundException e) {
            log.error("Registration failed due to invalid role: {}", e.getMessage(), e);
            throw new UserServiceLogicException("Registration failed: Invalid role provided!");
        } catch (DataAccessException e) {
            log.error("Registration failed while persisting user (database error): {}", e.getMessage(), e);
            throw new UserServiceLogicException("Registration failed: Database error. Please try again later!");
        } catch (IllegalArgumentException e) {
            log.error("Registration failed due to invalid input: {}", e.getMessage(), e);
            throw new UserServiceLogicException("Registration failed: Invalid input data. Please check your information!");
        } catch (Exception e) {
            log.error("Registration failed with unexpected error: {}", e.getMessage(), e);
            throw new UserServiceLogicException("Registration failed: Something went wrong! Error: " + e.getMessage());
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<?>> resetPassword(ResetPasswordRequestDto resetPasswordDto) throws UserNotFoundException, UserServiceLogicException {
        if (userService.existsByEmail(resetPasswordDto.getEmail())) {
            try {
                User user = userService.findByEmail(resetPasswordDto.getEmail());

                if (!resetPasswordDto.getCurrentPassword().isEmpty()) {
                    if (!passwordEncoder.matches(resetPasswordDto.getCurrentPassword(), user.getPassword())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(
                                ApiResponseStatus.FAILED,
                                HttpStatus.BAD_REQUEST,
                                "Reset password not successful: current password is incorrect!!"
                        ));
                    }
                }
                user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));

                userRepository.save(user);

                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
                        ApiResponseStatus.SUCCESS,
                        HttpStatus.CREATED,
                        "Reset successful: Password has been successfully reset!"
                ));
            } catch (DataAccessException | IllegalArgumentException e) {
                log.error("Resetting password failed while persisting user: {}", e.getMessage(), e);
                throw new UserServiceLogicException("Failed to reset your password: Try again later!");
            }
        }

        throw new UserNotFoundException("User not found with email " + resetPasswordDto.getEmail());
    }

    private User createUser(SignUpRequestDto signUpRequestDto) throws RoleNotFoundException {
        return new User(
                signUpRequestDto.getUserName(),
                signUpRequestDto.getEmail(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                true,
                determineRoles(signUpRequestDto.getRoles())
        );
    }

    private Set<Role> determineRoles(Set<String> strRoles) throws RoleNotFoundException {
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add(roleFactory.getInstance("user"));
        } else {
            for (String role : strRoles) {
                roles.add(roleFactory.getInstance(role));
            }
        }
        return roles;
    }



}