package com.ptit.iot.service;

import com.ptit.iot.dto.ProfileResponse;
import com.ptit.iot.entity.User;
import com.ptit.iot.repository.UserRepository;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponse getProfile(Authentication authentication) {
        String username = authentication == null ? "admin" : authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new ProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            "Student, Software Engineering",
            "B23DCCN885",
            "D23CNPM02",
            "PTIT (Học viện Công nghệ Bưu chính Viễn thông)",
            "Xin chào! Tôi là Lê Quốc Tuấn, sinh viên chuyên ngành Công nghệ phần mềm. Đồ án tập trung xây dựng hệ thống IoT giám sát nhiệt độ, độ ẩm, ánh sáng và điều khiển thiết bị qua Web.",
            Map.of(
                "phone", "0869728340",
                "email", "tuanthanhmc123@gmail.com",
                "address", "Hà Nội, Việt Nam"
            ),
            Map.of(
                "githubLink", user.getGithubLink(),
                "figmaLink", user.getFigmaLink(),
                "apiDocsLink", user.getApiDocsLink(),
                "baocaoLink", user.getBaocaoLink()
            )
        );
    }
}
