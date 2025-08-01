package com.deepanshu.devlog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.Project;
import com.deepanshu.devlog.Entity.ProjectMember;
import com.deepanshu.devlog.Entity.User;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    List<ProjectMember> findByProject(Project project);
    boolean existsByProjectAndUserUsername(Project project, String username);
    boolean existsByProjectIdAndUserUsername(Long projectId, String username);
}
