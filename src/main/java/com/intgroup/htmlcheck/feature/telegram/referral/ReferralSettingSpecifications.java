package com.intgroup.htmlcheck.feature.telegram.referral;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Iterator;

public class ReferralSettingSpecifications {
    public static Specification<ReferralSetting> any() {
        return (root, cq, cb) -> cb.isNotNull(root.get("id"));
    }

    public static Specification<ReferralSetting> idIs(long id) {
        return (root, cq, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<ReferralSetting> sourceTagIs(String sourceTag) {
        return (root, cq, cb) -> cb.equal(root.get("sourceTag"), sourceTag);
    }

    public static Specification<ReferralSetting> targetTagIs(String targetTag) {
        return (root, cq, cb) -> cb.equal(root.get("targetTag"), targetTag);
    }
    
    public static Specification<ReferralSetting> and(Specification<ReferralSetting> ... specifications) {
        return and(Arrays.asList(specifications));
    }

    public static Specification<ReferralSetting> and(Iterable<Specification<ReferralSetting>> specifications) {
        int index = 0;
        Iterator<Specification<ReferralSetting>> iterator = specifications.iterator();
        Specification<ReferralSetting> result = null;
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

    public static Specification<ReferralSetting> or(Specification<ReferralSetting> ... specifications) {
        return or(Arrays.asList(specifications));
    }

    public static Specification<ReferralSetting> or(Iterable<Specification<ReferralSetting>> specifications) {
        int index = 0;
        Iterator<Specification<ReferralSetting>> iterator = specifications.iterator();
        Specification<ReferralSetting> result = null;
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
