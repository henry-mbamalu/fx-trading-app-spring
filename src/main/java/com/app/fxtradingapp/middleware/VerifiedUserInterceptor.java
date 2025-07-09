package com.app.fxtradingapp.middleware;

import com.app.fxtradingapp.entity.User;
import com.app.fxtradingapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerifiedUserInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("VerifiedUserInterceptor hit!");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthenticated");
            return false;
        }

        UUID userId = UUID.fromString(authentication.getName()); // or cast authentication.getPrincipal()

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isVerified()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not verified");
            return false;
        }

        return true;
    }
}