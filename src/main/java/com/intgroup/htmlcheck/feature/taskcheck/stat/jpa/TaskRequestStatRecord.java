package com.intgroup.htmlcheck.feature.taskcheck.stat.jpa;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class TaskRequestStatRecord {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private LocalDateTime dateTime;

    @Column
    private float rps;
}
