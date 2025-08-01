package com.deepanshu.devlog.dto;

import java.time.LocalDateTime;

import com.deepanshu.devlog.Entity.Task;
import com.deepanshu.devlog.utils.TaskStatus;
import com.deepanshu.devlog.utils.Priority;


public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String assignedToUsername;
    private String projectName;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.assignedToUsername = task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null;
        this.projectName = task.getProject().getName();
        this.createdAt = task.getCreatedAt();
        this.dueDate = task.getDueDate();
    }
}
