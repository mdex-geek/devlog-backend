package com.deepanshu.devlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.TaskHistory;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
}