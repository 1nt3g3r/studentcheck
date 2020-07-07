package com.intgroup.htmlcheck.domain;

import com.intgroup.htmlcheck.domain.security.User;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user_preference")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "pref_name")
    private String name;

    @Column(name = "pref_value", length = 10000)
    private String value;
}
