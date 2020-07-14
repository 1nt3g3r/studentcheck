package com.intgroup.htmlcheck.feature.analytics;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class AnalyticsEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private LocalDateTime dateTime = LocalDateTime.now();
    
    @Column
    private String userEmail;
    
    @Column
    private String eventName;
    
    @Column
    private String eventValue;
    
    @Column
    private String cf1;
    
    @Column
    private String cf2;
    
    @Column
    private String cf3;
    
    @Column
    private String cf4;
}
