package com.deepanshu.devlog.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;

import com.deepanshu.devlog.Entity.Project;
import com.deepanshu.devlog.Entity.Task;
import com.deepanshu.devlog.Entity.TaskHistory;
import com.deepanshu.devlog.Entity.Notification;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.dto.TaskRequest;
import com.deepanshu.devlog.repository.NotificationRepository;
import com.deepanshu.devlog.repository.ProjectMemberRepository;
import com.deepanshu.devlog.repository.ProjectRepository;
import com.deepanshu.devlog.repository.TaskHistoryRepository;
import com.deepanshu.devlog.repository.TaskRepository;
import com.deepanshu.devlog.repository.UserRepository;
import com.deepanshu.devlog.utils.Priority;
import com.deepanshu.devlog.utils.TaskStatus;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
private NotificationRepository notificationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
private EmailService emailService;


    private String priority;

    public List<Task> getProjectTasks(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return taskRepository.findByProject(project);
    }

    public Task createTask(Long projectId, TaskRequest request, String username) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        boolean isMember = projectMemberRepository.existsByProjectAndUserUsername(project, username);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setDueDate(request.getDueDate());

        // Status (defaults to TODO if not provided)
        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus());
            }
        }

        // Priority (defaults to MEDIUM if not provided)
        if (request.getPriority() != null) {
            try {
                task.setPriority(Priority.valueOf(request.getPriority().name()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority");
            }
        } else {
            task.setPriority(Priority.MEDIUM);
        }

        // Assignee
        if (request.getAssignedToUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found"));
            task.setAssignedTo(assignedUser);

             // Email notification
        emailService.sendTaskAssignmentEmail(
        assignedUser.getEmail(),
        task.getTitle(),
        username // The user assigning the task
    );
        }

        if (request.getParentTaskId() != null) {
            Task parent = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent task not found"));
            task.setParentTask(parent);
        }

        Task savedTask = taskRepository.save(task);

        // Save history
        TaskHistory history = TaskHistory.builder()
                .action("CREATED")
                .performedBy(username)
                .task(savedTask)
                .timestamp(LocalDateTime.now())
                .build();

        taskHistoryRepository.save(history);

        return savedTask;

    }

    public Task updateTask(Long projectId, Long taskId, TaskRequest request, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        boolean isMember = projectMemberRepository.existsByProjectAndUserUsername(project, username);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        if (request.getTitle() != null)
            task.setTitle(request.getTitle());
        if (request.getDescription() != null)
            task.setDescription(request.getDescription());
        if (request.getDueDate() != null)
            task.setDueDate(request.getDueDate());

        // Status update
        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
            }
        }

        // Priority update
        if (request.getPriority() != null) {
            try {
                task.setPriority(Priority.valueOf(request.getPriority().name()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority value");
            }
        }

        // Reassign task if assignedToUserId is present
        if (request.getAssignedToUserId() != null) {
            User newAssignee = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned user not found"));
            task.setAssignedTo(newAssignee);
        }

    Task updatedTask = taskRepository.save(task);

    if (task.getAssignedTo() != null && !task.getAssignedTo().getUsername().equals(username)) {
        Notification notification = Notification.builder()
                .message("You have been assigned a new task: " + task.getTitle())
                .recipient(task.getAssignedTo())
                .build();
        notificationRepository.save(notification);
    }
    return updatedTask;
}

    public void deleteTask(Long projectId, Long taskId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!task.getProject().getId().equals(project.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not belong to the given project");
        }

        boolean isMember = projectMemberRepository.existsByProjectAndUserUsername(project, username);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        taskRepository.delete(task);
    }

    public List<Task> filterTasks(Long projectId, String status, String priority, Long assignedToUserId,
            String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        boolean isMember = projectMemberRepository.existsByProjectAndUserUsername(project, username);
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        return taskRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("project"), project));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), TaskStatus.valueOf(status.toUpperCase())));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), Priority.valueOf(priority.toUpperCase())));
            }
            if (assignedToUserId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToUserId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

}
