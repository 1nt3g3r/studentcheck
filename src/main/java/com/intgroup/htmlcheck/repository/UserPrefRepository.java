package com.intgroup.htmlcheck.repository;

import com.intgroup.htmlcheck.domain.UserPreference;
import com.intgroup.htmlcheck.domain.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPrefRepository extends JpaRepository<UserPreference, Long> {
    @Query("from UserPreference p where p.user.id = :id and p.name = :prefName")
    UserPreference getOption(@Param(value = "id") long id, @Param(value = "prefName") String prefName);

    @Query("from User u where u.id in (select p.user.id from UserPreference p where p.name=:optionName and p.value=:optionValue)")
    List<User> getUsersWithOption(@Param("optionName") String optionName, @Param("optionValue") String optionValue);
}
