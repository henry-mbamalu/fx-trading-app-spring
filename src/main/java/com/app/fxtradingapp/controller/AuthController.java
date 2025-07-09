package com.app.fxtradingapp.controller;

import com.app.fxtradingapp.dto.user.LoginDto;
import com.app.fxtradingapp.dto.user.RegisterDto;
import com.app.fxtradingapp.dto.user.VerifyOtpDto;
import com.app.fxtradingapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        System.out.println(loginDto);
        return this.authService.login(loginDto);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto registerDto) {
        return this.authService.registerUser(registerDto);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDto verifyOtpDto) {
        return this.authService.verifyOtp(verifyOtpDto);
    }
}