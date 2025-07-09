package com.app.fxtradingapp.filter;


import com.app.fxtradingapp.dto.ResponseDto;
import com.app.fxtradingapp.entity.User;
import com.app.fxtradingapp.repository.UserRepository;
import com.app.fxtradingapp.service.impl.CustomUserDetailsServiceImpl;
import com.app.fxtradingapp.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class UserVerificationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    public UserVerificationFilter(JwtUtil jwtUtil,
                                  CustomUserDetailsServiceImpl userDetailsService,
                                  UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);

            try {
                UUID userId = jwtUtil.extractUserId(jwt);

                // Check if user is verified
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && !user.isVerified()) {
                    sendErrorResponse(response,
                            HttpServletResponse.SC_FORBIDDEN,
                            "User is not verified. Please verify your email.");
                    return;
                }

                // Continue with the existing authentication logic
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails =  userDetailsService.loadUserById(userId);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                sendErrorResponse(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int statusCode,
                                   String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResponseDto errorResponse = new ResponseDto();
        errorResponse.setMessage(message);
        String jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}