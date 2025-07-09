package com.app.fxtradingapp.service;

import com.app.fxtradingapp.dto.ResponseDto;
import com.app.fxtradingapp.dto.user.LoginDto;
import com.app.fxtradingapp.dto.user.RegisterDto;
import com.app.fxtradingapp.dto.user.VerifyOtpDto;
import com.app.fxtradingapp.entity.User;
import com.app.fxtradingapp.repository.UserRepository;
import com.app.fxtradingapp.util.JwtUtil;
import com.app.fxtradingapp.util.LocaleHandler;
import com.app.fxtradingapp.util.ResponseCodes;
import com.app.fxtradingapp.util.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ResponseEntity<?> registerUser(RegisterDto registerDto){
        ResponseDto response = new ResponseDto();

        try {

            if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
                response.setMessage(LocaleHandler.getMessage(ResponseCodes.USER_ALREADY_EXIST));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String otp = String.valueOf((int)(Math.random() * 900000 + 100000));
            LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(10); // expires in 10 minutes

            User user = new User();
            user.setFirstName(registerDto.getFirstName());
            user.setLastName(registerDto.getLastName());
            user.setEmail(registerDto.getEmail());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setRole(Roles.USER);
            user.setOtp(otp);
            user.setOtpExpiresAt(otpExpiry);
            user.setCreatedAt(LocalDateTime.now());


            userRepository.save(user);

            String subject = "Email Verification";
            String text = "Hello " + user.getFirstName() + ",\n\nYour verification OTP is: " + otp;
            mailService.sendMail(user.getEmail(), subject, text);

            response.setMessage("User registered successfully! Please check your email for the OTP.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {

            response.setMessage(LocaleHandler.getMessage(ResponseCodes.SYSTEM_ERROR));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }


    };

    public ResponseEntity<?> login(LoginDto loginDto){
        ResponseDto response = new ResponseDto();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            User user = userRepository.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Add custom claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", user.getRole());
            // Generate JWT token
            final String jwt = jwtUtil.generateToken(user.getId(), claims);


            if (jwt.isEmpty()) {
                response.setMessage("Failed to create token");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.setMessage(LocaleHandler.getMessage(ResponseCodes.SUCCESS));
            response.setData(Map.of(
                    "token", jwt,
                    "user", user
            ));
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (BadCredentialsException e) {
            response.setMessage("Invalid email or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (UsernameNotFoundException e) {
            response.setMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_FOUND));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            System.out.println(e);
            response.setMessage("An error occurred during login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<?> verifyOtp(VerifyOtpDto verifyOtpDto){
        ResponseDto response = new ResponseDto();
        try{
            Optional<User> optionalUser = userRepository.findByEmail(verifyOtpDto.getEmail());
            if (optionalUser.isEmpty()) {
                response.setMessage(LocaleHandler.getMessage(ResponseCodes.USER_NOT_FOUND));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = optionalUser.get();

            if (user.getOtp() == null || user.getOtpExpiresAt() == null) {
                response.setMessage("No OTP requested");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
                response.setMessage("OTP has expired");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (!user.getOtp().equals(verifyOtpDto.getOtp())) {
                response.setMessage(LocaleHandler.getMessage(ResponseCodes.INVALID_OTP));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            user.setVerified(true);
            user.setOtp(null);
            user.setOtpExpiresAt(null);
            userRepository.save(user);

            response.setMessage("Email verified successfully!");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.setMessage(LocaleHandler.getMessage(ResponseCodes.SYSTEM_ERROR));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

