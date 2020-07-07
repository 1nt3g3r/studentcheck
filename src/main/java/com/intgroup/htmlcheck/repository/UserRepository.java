package com.intgroup.htmlcheck.repository;


import com.intgroup.htmlcheck.domain.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByToken(String token);

    @Query("SELECT (count(u.id) > 0) from User u WHERE u.email = :email")
    boolean userWithEmailExists(@Param("email") String email);
}