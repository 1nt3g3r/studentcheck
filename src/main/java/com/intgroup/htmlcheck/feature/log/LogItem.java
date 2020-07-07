package com.intgroup.htmlcheck.feature.log;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class LogItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column
    private LocalDateTime time = LocalDateTime.now();

    @Column
    private String tag;

    @Column
    private String value;
}
