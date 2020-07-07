package com.intgroup.htmlcheck.feature.telegram.im;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstantMessageRepository extends JpaRepository<InstantMessage, Long>, JpaSpecificationExecutor<InstantMessage> {
    @Query("select distinct(im.telegramUserId) from InstantMessage im where im.seen = false")
    List<Long> getTelegramUserIdsWithUnseenMessages();

    @Query(nativeQuery=true, value = "select count(id) from instant_message where telegram_user_id = :telegramUserId and seen = false")
    int getUnseenMessageCount(@Param("telegramUserId") long telegramUserId);

    @Query(nativeQuery=true, value = "" +
            "select telegram_user_id, count(id) " +
            "from instant_message " +
            "where telegram_user_id in :telegramUserIds and seen = false " +
            "group by telegram_user_id;")
    long[][] getUnseenMessageCountForUsers(@Param("telegramUserIds") List<Long> telegramUserIds);
}
