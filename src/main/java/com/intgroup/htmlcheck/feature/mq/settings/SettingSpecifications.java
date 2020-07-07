package com.intgroup.htmlcheck.feature.mq.settings;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class SettingSpecifications {
    public static Specification<Setting> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<Setting> nameNotEmpty() {
        return (root, cq, cb) -> cb.and(cb.isNotNull(root.get("name")), cb.notEqual(root.get("name"), ""));
    }
    public static Specification<Setting> nameIs(String name) {
        return (root, cq, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<Setting> dateIs(String date) {
        return (root, cq, cb) -> cb.equal(root.get("date"), date);
    }

    public static Specification<Setting> tagIs(String tag) {
        return (root, cq, cb) -> cb.equal(root.get("tag"), tag);
    }

    public static Specification<Setting> dateAndTagAreEmpty() {
        return (root, cq, cb) ->
            cb.and(
                    cb.or(cb.isNull(root.get("tag")), cb.notEqual(root.get("tag"), "")),
                    cb.or(cb.isNull(root.get("date")), cb.notEqual(root.get("date"), ""))
            );
    }

    public static Specification<Setting> idIs(long id) {
        return (root, cq, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Setting> query(String query) {
        String likeString = "%" + query.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(root.get("name"), likeString),
                cb.like(cb.lower(root.get("date")), likeString),
                cb.like(cb.lower(root.get("tag")), likeString)
        );
    }

    public static Specification<Setting> and(Specification<Setting> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<Setting> and(Iterable<Specification<Setting>> specifications) {
        int index = 0;
        Iterator<Specification<Setting>> iterator = specifications.iterator();
        Specification<Setting> result = null;
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

    public static Specification<Setting> or(Specification<Setting> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<Setting> or(Iterable<Specification<Setting>> specifications) {
        int index = 0;
        Iterator<Specification<Setting>> iterator = specifications.iterator();
        Specification<Setting> result = null;
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
