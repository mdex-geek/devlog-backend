package com.deepanshu.devlog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.Project;

import com.deepanshu.devlog.Entity.User;

// Todo: this is my blog project, so I will write comments in my own way.
// why we use JpaRepository<Project, Long>?
// Because we want to perform CRUD operations on the Project entity and the ID type is Long.
// the why we not use CrudRepository<Project, Long>?
// Because JpaRepository extends CrudRepository and provides additional JPA-specific methods.
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // why we use List<Project> why not Project?
    // Because a user can have multiple projects, so we return a list of projects.
    List<Project> findByOwner(User owner);

}
