package com.intgroup.htmlcheck.feature.telegram.domain;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.im.InstantMessage;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TelegramUserSpecifications {
    public static Specification<TelegramUser> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("userId"));
    }

    public static Specification<TelegramUser> phoneInList(List<String> phones) {
        if (phones.isEmpty()) {
            return (root, cq, cb) -> cb.equal(root.get("userId"), -1);
        } else {
            return (root, cq, cb) -> root.get("phone").in(phones);
        }
    }

    public static Specification<TelegramUser> userIdInList(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return (root, cq, cb) -> cb.equal(root.get("userId"), -1);
        } else {
            return (root, cq, cb) -> root.get("userId").in(userIds);
        }
    }

    public static Specification<TelegramUser> tagIs(String tag) {
        return (root, cq, cb) -> cb.equal(root.get("tag"), tag);
    }

    public static Specification<TelegramUser> eventDateIs(String eventDate) {
        return (root, cq, cb) -> cb.equal(root.get("eventDate"), eventDate);
    }

    public static Specification<TelegramUser> phoneIs(String phone) {
        return (root, cq, cb) -> cb.equal(root.get("phone"), phone);
    }

    public static Specification<TelegramUser> userIdIs(long userId) {
        return (root, cq, cb) -> cb.equal(root.get("userId"), userId);
    }
    
    public static Specification<TelegramUser> fromUserIdIs(long fromUserId) {
        return (root, cq, cb) -> cb.equal(root.get("fromUserId"), fromUserId);
    }

    public static Specification<TelegramUser> chatIdIs(String chatId) {
        return (root, cq, cb) -> cb.equal(root.get("chatId"), chatId);
    }

    public static Specification<TelegramUser> dailyNotifyHourIs(int hour) {
        return (root, cq, cb) -> cb.equal(root.get("dailyNotifyHour"), hour);
    }

    public static Specification<TelegramUser> phoneIsNotNull() {
        return (root, cq, cb) -> cb.isNotNull(root.get("phone"));
    }

    public static Specification<TelegramUser> adminRole() {
        return (root, cq, cb) -> cb.equal(root.get("role"), "admin");
    }

    public static Specification<TelegramUser> query(String query) {
        String likeString = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
          cb.like(root.get("phone"), likeString),
          cb.like(cb.lower(root.get("userName")), likeString),
          cb.like(cb.lower(root.get("firstName")), likeString),
          cb.like(cb.lower(root.get("lastName")), likeString),
          cb.like(cb.lower(root.get("email")), likeString),
          cb.like(cb.lower(root.get("metadata")), likeString)
        );
    }

    public static Specification<TelegramUser> and(Specification<TelegramUser> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<TelegramUser> and(Iterable<Specification<TelegramUser>> specifications) {
        int index = 0;
        Iterator<Specification<TelegramUser>> iterator = specifications.iterator();
        Specification<TelegramUser> result = null;
        while(iterator.hasNext()) {
            if (index == 0) {
                result = Specification.where(iterator.next());
            } else {
                result = result.and(iterator.next());
            }
            index++;
        }
        return result;
    }

    public static Specification<TelegramUser> or(Specification<TelegramUser> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<TelegramUser> or(Iterable<Specification<TelegramUser>> specifications) {
        int index = 0;
        Iterator<Specification<TelegramUser>> iterator = specifications.iterator();
        Specification<TelegramUser> result = null;
        while(iterator.hasNext()) {
            if (index == 0) {
                result = Specification.where(iterator.next());
            } else {
                result = result.or(iterator.next());
            }
            index++;
        }
        return result;
    }
}
