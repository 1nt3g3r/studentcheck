package com.intgroup.htmlcheck.feature.telegram.im;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class InstantMessageSpecifications {
    public static Specification<InstantMessage> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<InstantMessage> isIs(long id) {
        return (root, cq, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<InstantMessage> telegramUserIdIs(long telegramUserId) {
        return (root, cq, cb) -> cb.equal(root.get("telegramUserId"), telegramUserId);
    }

    public static Specification<InstantMessage> seen(boolean seen) {
        return (root, cq, cb) -> cb.equal(root.get("seen"), seen);
    }

    public static Specification<InstantMessage> messageTypeIs(InstantMessageType type) {
        return (root, cq, cb) -> cb.equal(root.get("messageType"), type);
    }

    public static Specification<InstantMessage> query(String query) {
        String likeString = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(root.get("message"), likeString);
    }

    public static Specification<InstantMessage> and(Specification<InstantMessage> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<InstantMessage> and(Iterable<Specification<InstantMessage>> specifications) {
        int index = 0;
        Iterator<Specification<InstantMessage>> iterator = specifications.iterator();
        Specification<InstantMessage> result = null;
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

    public static Specification<InstantMessage> or(Specification<InstantMessage> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<InstantMessage> or(Iterable<Specification<InstantMessage>> specifications) {
        int index = 0;
        Iterator<Specification<InstantMessage>> iterator = specifications.iterator();
        Specification<InstantMessage> result = null;
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
