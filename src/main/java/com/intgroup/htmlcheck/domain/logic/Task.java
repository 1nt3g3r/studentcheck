package com.intgroup.htmlcheck.domain.logic;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "task")
public class Task {
    @Column(name = "id", length = 150)
    @Id
    private String id;

    @Column(name = "task_type")
    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Column(name = "content", length = 50000)
    @Lob
    private String content;
}
