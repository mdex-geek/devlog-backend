package com.deepanshu.devlog.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action; //created ,updated etc
    private String performedBy;

    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    private Task task;
}

