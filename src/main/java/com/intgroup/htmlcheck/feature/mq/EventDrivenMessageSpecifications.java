package com.intgroup.htmlcheck.feature.mq;

import com.intgroup.htmlcheck.feature.mq.enums.Event;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class EventDrivenMessageSpecifications {
    public static Specification<EventDrivenMessage> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<EventDrivenMessage> concreteDateIs(String date) {
        return (root, cq, cb) -> cb.equal(root.get("concreteDate"), date);
    }

    public static Specification<EventDrivenMessage> eventIs(Event event) {
        return (root, cq, cb) -> cb.equal(root.get("event"), event);
    }

    public static Specification<EventDrivenMessage> dayIs(int day) {
        return (root, cq, cb) -> cb.equal(root.get("day"), day);
    }

    public static Specification<EventDrivenMessage> hourIs(int hour) {
        return (root, cq, cb) -> cb.equal(root.get("hour"), hour);
    }

    public static Specification<EventDrivenMessage> minuteIs(int minute) {
        return (root, cq, cb) -> cb.equal(root.get("minute"), minute);
    }

    public static Specification<EventDrivenMessage> tagIs(String tag) {
        return (root, cq, cb) -> cb.equal(root.get("tag"), tag);
    }

    public static Specification<EventDrivenMessage> userButtonPayloadIs(String payload) {
        return (root, cq, cb) -> cb.equal(root.get("userButtonPayload"), payload);
    }

    public static Specification<EventDrivenMessage> idIs(long id) {
        return (root, cq, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<EventDrivenMessage> query(String query) {
        String likeString = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(root.get("title"), likeString),
                cb.like(cb.lower(root.get("text")), likeString),
                cb.like(cb.lower(root.get("tag")), likeString)
        );
    }

    public static Specification<EventDrivenMessage> and(Specification<EventDrivenMessage> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<EventDrivenMessage> and(Iterable<Specification<EventDrivenMessage>> specifications) {
        int index = 0;
        Iterator<Specification<EventDrivenMessage>> iterator = specifications.iterator();
        Specification<EventDrivenMessage> result = null;
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

    public static Specification<EventDrivenMessage> or(Specification<EventDrivenMessage> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<EventDrivenMessage> or(Iterable<Specification<EventDrivenMessage>> specifications) {
        int index = 0;
        Iterator<Specification<EventDrivenMessage>> iterator = specifications.iterator();
        Specification<EventDrivenMessage> result = null;
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
