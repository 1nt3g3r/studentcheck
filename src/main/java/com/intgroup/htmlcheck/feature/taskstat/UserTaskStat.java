package com.intgroup.htmlcheck.feature.taskstat;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class UserTaskStat {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String userId;

    @Column
    private String taskId;

    @Column
    private int solveTimeSeconds;

    @Column
    private int solveTryCount = 0;

    @Column
    private boolean solved = false;

    @Column
    private LocalDateTime solveDateTime;
}
