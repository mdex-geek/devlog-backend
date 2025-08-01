package com.deepanshu.devlog.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.deepanshu.devlog.Entity.Project;
import com.deepanshu.devlog.Entity.ProjectMember;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.repository.ProjectMemberRepository;
import com.deepanshu.devlog.repository.ProjectRepository;
import com.deepanshu.devlog.repository.UserRepository;
import com.deepanshu.devlog.utils.MemberRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
    @Autowired
    private final ProjectRepository projectRepository;
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public Project createProject(String name,String description,String username) {
        User owner = userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User not found") );

        Project project = Project.builder()
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .owner(owner)
                .build();
        return projectRepository.save(project);
    }

    public List<Project> getProjectsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User not found"));
        return projectRepository.findByOwner(user);
    }

    public void deleteProject(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Project not found"));

        if (!project.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unauthorized to delete this project");
        }

        projectRepository.delete(project);
    }

    public void addMemberToProject(Long projectId, String username, MemberRole role) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Project not found"));

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User not found"));

    boolean alreadyMember = projectMemberRepository.findByProjectAndUser(project, user).isPresent();
    if (alreadyMember) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of the project");
    }

    ProjectMember member = new ProjectMember();
    member.setProject(project);
    member.setUser(user);
    member.setRole(role);

    projectMemberRepository.save(member);
}
}
