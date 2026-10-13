package com.ptit.iot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "github_link", length = 500)
    private String githubLink;

    @Column(name = "figma_link", length = 500)
    private String figmaLink;

    @Column(name = "apidocs_link", length = 500)
    private String apiDocsLink;

    @Column(name = "baocao_link", length = 500)
    private String baocaoLink;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }

    public String getFigmaLink() {
        return figmaLink;
    }

    public void setFigmaLink(String figmaLink) {
        this.figmaLink = figmaLink;
    }

    public String getApiDocsLink() {
        return apiDocsLink;
    }

    public void setApiDocsLink(String apiDocsLink) {
        this.apiDocsLink = apiDocsLink;
    }

    public String getBaocaoLink() {
        return baocaoLink;
    }

    public void setBaocaoLink(String baocaoLink) {
        this.baocaoLink = baocaoLink;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
