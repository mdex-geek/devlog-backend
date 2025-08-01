package com.deepanshu.devlog.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskId(Long taskId);
}
