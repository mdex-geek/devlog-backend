package com.deepanshu.devlog.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.deepanshu.devlog.Entity.Project;
import com.deepanshu.devlog.Entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> ,JpaSpecificationExecutor<Task>{
    List<Task> findByProject(Project project);
}