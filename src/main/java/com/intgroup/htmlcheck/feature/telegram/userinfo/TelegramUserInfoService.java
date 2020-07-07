package com.intgroup.htmlcheck.feature.telegram.userinfo;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.service.ImportMetadataService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class TelegramUserInfoService {
    private static final int MAX_CACHE_SIZE = 1000;

    private Map<String, TelegramUserInfo> cache;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private ImportMetadataService importMetadataService;

    @PostConstruct
    public void init() {
        cache = new LinkedHashMap<>();
    }

    public void setInfo(String phone, String firstName, String email, String metadata) {
        //If cache full - remove first element
        if (cache.size() > MAX_CACHE_SIZE) {
            String toRemove = cache.keySet().iterator().next();
            cache.remove(toRemove);
        }

        //Save data in cache
        cache.put(phone, new TelegramUserInfo(firstName, email, metadata));

        //Update user
        updateUser(phone);
    }

    public void updateUser(String phone) {
        if (!cache.containsKey(phone)) {
            return;
        }

        TelegramUser user = telegramUserService.getByPhoneNumber(phone);
        if (user == null) {
            return;
        }

        TelegramUserInfo userInfo = cache.get(phone);

        if (userInfo.getFirstName() != null && !userInfo.getFirstName().isBlank()) {
            user.setFirstName(userInfo.getFirstName());
        }

        if (userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            user.setEmail(userInfo.getEmail());
        }

        if (userInfo.getMetadata() != null && !userInfo.getMetadata().isBlank()) {
            String[] metadataArray = userInfo.getMetadata().replace("\r", "").split("\n");
            importMetadataService.importMetadata(Arrays.asList(phone), new LinkedHashSet<>(Arrays.asList(metadataArray)));
        }

        telegramUserService.save(user);

        cache.remove(phone);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TelegramUserInfo {
        private String metadata;
        private String firstName;
        private String email;
    }
}
