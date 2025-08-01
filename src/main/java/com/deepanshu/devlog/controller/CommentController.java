package com.deepanshu.devlog.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.deepanshu.devlog.Entity.Comment;
import com.deepanshu.devlog.Entity.Task;
import com.deepanshu.devlog.Entity.User;
import com.deepanshu.devlog.dto.CommentRequest;
import com.deepanshu.devlog.repository.CommentRepository;
import com.deepanshu.devlog.repository.TaskRepository;
import com.deepanshu.devlog.repository.UserRepository;


@RestController
@RequestMapping("/api/v1/tasks/{taskId}/comments")
public class CommentController {
     @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Comment addComment(@PathVariable Long taskId, @RequestBody CommentRequest request, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setAuthor(user);
        comment.setTask(task);

        return commentRepository.save(comment);
    }

    @GetMapping
    public List<Comment> getComments(@PathVariable Long taskId) {
        return commentRepository.findByTaskId(taskId);
    }
}
