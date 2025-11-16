package com.fullStack.expenseTracker.controllers;

import com.fullStack.expenseTracker.services.AuthService;
import com.fullStack.expenseTracker.dto.reponses.ApiResponseDto;
import com.fullStack.expenseTracker.dto.requests.ResetPasswordRequestDto;
import com.fullStack.expenseTracker.exceptions.UserNotFoundException;
import com.fullStack.expenseTracker.exceptions.UserServiceLogicException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/mypockit/auth/forgotPassword")
public class ForgotPasswordController {

    @Autowired
    private AuthService authService;

    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponseDto<?>> resetPassword(@RequestBody @Valid ResetPasswordRequestDto resetPasswordDto)
            throws UserNotFoundException, UserServiceLogicException {
        return authService.resetPassword(resetPasswordDto);
    }
}
