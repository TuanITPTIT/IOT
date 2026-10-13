package com.ptit.iot.service;

import com.ptit.iot.dto.LoginRequest;
import com.ptit.iot.dto.LoginResponse;
import com.ptit.iot.dto.UserSummaryDto;
import com.ptit.iot.entity.User;
import com.ptit.iot.repository.UserRepository;
import com.ptit.iot.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtTokenProvider.generateToken(authentication);
        return new LoginResponse(
            token,
            "Bearer",
            new UserSummaryDto(user.getId(), user.getUsername(), user.getFullName(), user.getRole())
        );
    }
}
