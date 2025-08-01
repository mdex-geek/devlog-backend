package com.deepanshu.devlog.dto;

import java.time.LocalDateTime;

import com.deepanshu.devlog.utils.Priority;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private String title;
    private String description;
    private String status;
    private Priority priority;
    private Long assignedToUserId;
    private Long projectId;
    private LocalDateTime dueDate;
    private Long parentTaskId; 
}
