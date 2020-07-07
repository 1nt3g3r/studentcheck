package com.intgroup.htmlcheck.domain.security;

import com.intgroup.htmlcheck.domain.logic.TaskType;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "active")
    private int active;

    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Role> roles;

    @Column(name = "token")
    private String token;

//    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//    @ElementCollection
//    private Set<String> passedTasks;

    @Column(name = "phone")
    private String phone;

    @Column
    private LocalDateTime lastActionTime;

    public String getRolesAsString() {
        StringJoiner joiner = new StringJoiner(",");
        for(Role role: getRoles()) {
            joiner.add(role.getRole());
        }
        return joiner.toString();
    }

    public boolean isAdmin() {
        for(Role role: getRoles()) {
            if (role.getRole().equalsIgnoreCase("admin")) {
                return true;
            }
        }

        return false;
    }

}