package com.deepanshu.devlog.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepanshu.devlog.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Additional query methods can be defined here if needed
   Optional<User> findByUsername(String username);

}
