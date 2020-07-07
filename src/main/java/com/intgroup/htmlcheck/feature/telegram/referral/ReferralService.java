package com.intgroup.htmlcheck.feature.telegram.referral;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserSpecifications;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReferralService {
    @Autowired
    private ReferralSettingRepository referralSettingRepository;
    
    @Autowired
    private TelegramUserService telegramUserService;

    /**
     * Return TelegramUser, who invited referral
     */
    public TelegramUser getWhoInvited(TelegramUser referral) {
        if (referral.getFromUserId() <= 0) {
            return null;
        }
        
        return telegramUserService.queryFirstOrNull(TelegramUserSpecifications.userIdIs(referral.getFromUserId()));    
    }

    /**
     * Return all users that current user invited
     */
    public List<TelegramUser> getInvitedUsers(TelegramUser user) {
        return telegramUserService.queryAll(TelegramUserSpecifications.fromUserIdIs(user.getUserId()));
    }
    
    public void processReferral(long fromUserId, TelegramUser user) {
        TelegramUser fromUser = telegramUserService.getById(fromUserId);
        
        if (fromUser == null) {
            return;
        }
        
        String sourceTag = fromUser.getTag();
        if (sourceTag == null || sourceTag.isBlank()) {
            return;
        }
        
        ReferralSetting referralSetting = queryFirstOrNull(ReferralSettingSpecifications.sourceTagIs(sourceTag));
        if (referralSetting == null) {
            return;
        }

        //Save from
        user.setFromUserId(fromUserId);
        
        //Save tag
        String targetTag = referralSetting.getTargetTag();
        if (targetTag != null && !targetTag.isBlank()) {
            user.setTag(targetTag);
        }
    }

    public void delete(Specification<ReferralSetting> specification) {
        referralSettingRepository.deleteAll(queryAll(specification));
    }

    public int count(Specification<ReferralSetting> specification) {
        return (int) referralSettingRepository.count(specification);
    }
    
    public ReferralSetting queryFirstOrNull(Specification<ReferralSetting> specification) {
        List<ReferralSetting> referralSettings = queryAll(specification);
        
        if (referralSettings.isEmpty()) {
            return null;
        }
        
        return referralSettings.get(0);
    }

    public List<ReferralSetting> queryAll(Specification<ReferralSetting> specification) {
        return referralSettingRepository.findAll(specification);
    }

    public List<ReferralSetting> queryPage(int pageIndex, Specification<ReferralSetting> specification) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, PaginationService.PAGE_SIZE, sort);
        Page<ReferralSetting> page = referralSettingRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public void deleteById(long id) {
        if (referralSettingRepository.existsById(id)) {
            referralSettingRepository.deleteById(id);
        }
    }

    public ReferralSetting getById(long id) {
        if (referralSettingRepository.existsById(id)) {
            return referralSettingRepository.getOne(id);
        }

        return null;
    }

    public void save(ReferralSetting instantMessage) {
        referralSettingRepository.save(instantMessage);
    }
}
