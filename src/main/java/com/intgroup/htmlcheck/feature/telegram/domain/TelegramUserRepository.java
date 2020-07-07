package com.intgroup.htmlcheck.feature.telegram.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long>, JpaSpecificationExecutor<TelegramUser> {
    @Query("select distinct(u.tag) from TelegramUser u")
    List<String> getUniqueTags();

    @Query("select distinct(u.eventDate) from TelegramUser u")
    List<String> getUniqueEventDates();

    @Query("from TelegramUser t where t.userId in (:ids)")
    List<TelegramUser> getAllWithIds(@Param("ids") List<Long> ids);
}
