package com.deepanshu.devlog.controller;

import java.time.LocalDateTime;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepanshu.devlog.Entity.Project;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.dto.AddMemberRequest;
import com.deepanshu.devlog.repository.ProjectRepository;
import com.deepanshu.devlog.repository.UserRepository;
import com.deepanshu.devlog.service.JwtService;
import com.deepanshu.devlog.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository; // To fetch owner by username
    private final JwtService jwtService; // To extract username from token

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Project project, HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(projectRepository.save(project));
    }

    @GetMapping
    public ResponseEntity<?> getMyProjects(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(projectRepository.findByOwner(owner));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Project updatedProject) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setName(updatedProject.getName());
                    project.setDescription(updatedProject.getDescription());
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method to extract token from Authorization header
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<String> addMemberToProject(@PathVariable Long projectId,
                                                 @RequestBody AddMemberRequest request) {
    projectService.addMemberToProject(projectId, request.getUsername(), request.getRole());
    return ResponseEntity.ok("Member added successfully");
    }
}
