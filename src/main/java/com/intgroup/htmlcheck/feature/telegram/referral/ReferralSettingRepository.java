package com.intgroup.htmlcheck.feature.telegram.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralSettingRepository extends JpaRepository<ReferralSetting, Long>, JpaSpecificationExecutor<ReferralSetting> {
}
