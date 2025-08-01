package com.deepanshu.devlog.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deepanshu.devlog.Entity.Task;
import com.deepanshu.devlog.dto.TaskRequest;
import com.deepanshu.devlog.service.TaskService;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public Task createTask(@PathVariable Long projectId ,@RequestBody TaskRequest request, Principal principal) {
        return taskService.createTask(projectId,request, principal.getName());
    }

    @GetMapping
    public List<Task> getTasks(@PathVariable Long projectId) {
        return taskService.getProjectTasks(projectId);
    }

    @PutMapping("/{taskId}")
    public Task updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskRequest request,
            Principal principal) {
        return taskService.updateTask(projectId, taskId, request, principal.getName());
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(
        @PathVariable Long projectId,
        @PathVariable Long taskId,
        Principal principal) {
    taskService.deleteTask(projectId, taskId, principal.getName());
    }   

    @GetMapping("/filter")
    public List<Task> filterTasks(
        @PathVariable Long projectId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String priority,
        @RequestParam(required = false) Long assignedToUserId,
        Principal principal) {
    return taskService.filterTasks(projectId, status, priority, assignedToUserId, principal.getName());
}

}
