package com.ptit.iot.controller;

import com.ptit.iot.dto.ApiResponse;
import com.ptit.iot.dto.ProfileResponse;
import com.ptit.iot.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile(Authentication authentication) {
        return ApiResponse.success("User profile", userService.getProfile(authentication));
    }
}
