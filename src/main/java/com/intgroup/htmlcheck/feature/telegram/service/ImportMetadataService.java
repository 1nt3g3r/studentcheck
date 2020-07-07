package com.intgroup.htmlcheck.feature.telegram.service;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserSpecifications;
import com.intgroup.htmlcheck.service.util.NormalizeDataService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportMetadataService {
    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private NormalizeDataService normalizeDataService;

    public ImportMetadataResult importMetadata(List<String> phones, Set<String> metadata) {
        ImportMetadataResult result = new ImportMetadataResult();

        //Clean & normalize phones. Remove duplicates
        phones = phones.stream()
                .filter(phone -> phone != null && !phone.isBlank())
                .map(phone -> normalizeDataService.normalizePhone(phone))
                .collect(Collectors.toList());
        Set<String> cleanedPhones = new LinkedHashSet<>(phones);

        result.uniqueCount = cleanedPhones.size();
        result.duplicateCount = phones.size() - cleanedPhones.size();

        //Clean & normalize metadata
        Set<String> cleanMetadata = metadata.stream()
                .filter(meta -> meta != null && !meta.isBlank())
                .collect(Collectors.toSet());

        //Obtain users
        Map<String, TelegramUser> telegramUsers = new LinkedHashMap<>();
        telegramUserService
                .queryAll(TelegramUserSpecifications.phoneInList(phones))
                .forEach(telegramUser -> telegramUsers.put(telegramUser.getPhone(), telegramUser));

        //Update each user
        cleanedPhones.forEach(phone -> {
            TelegramUser telegramUser = telegramUsers.get(phone);

            if (telegramUser == null) {
                result.notFound.add(phone);
            } else {
                boolean userUpdated = false;

                List<String> userMetadata = getMetdataAsList(telegramUser);
                for(String item: cleanMetadata) {
                    if (!userMetadata.contains(item)) {
                        userUpdated = true;
                        userMetadata.add(item);
                    }
                }

                if (userUpdated) {
                    telegramUser.setMetadata(getMetadataAsString(userMetadata));
                    telegramUserService.save(telegramUser);

                    result.updated.add(phone);
                } else {
                    result.ignored.add(phone);
                }

            }
        });

        return result;
    }

    private List<String> getMetdataAsList(TelegramUser user) {
        String metadata = user.getMetadata();
        if (metadata == null) {
            metadata = "";
        }

        return Arrays
                .stream(metadata.replace("\r", "").split("\n"))
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toList());
    }

    private String getMetadataAsString(List<String> metadata) {
        StringJoiner result = new StringJoiner("\n");
        metadata.forEach(result::add);
        return result.toString();
    }

    @Data
    public static class ImportMetadataResult {
        private int uniqueCount;
        private int duplicateCount;

        private List<String> updated = new ArrayList<>();
        private List<String> ignored = new ArrayList<>();
        private List<String> notFound = new ArrayList<>();

        public String getUpdatedAsString() {
            return getListAsString(updated);
        }

        public String getIgnoredAsString() {
            return getListAsString(ignored);
        }

        public String getNotFoundAsString() {
            return getListAsString(notFound);
        }

        private String getListAsString(List<String> list) {
            StringJoiner result = new StringJoiner("\n");
            list.forEach(result::add);
            return result.toString();
        }
    }

    public static void main(String[] args) {
        List<String> result = Arrays.asList("1", "2", "3", "", "5");
        result = result.stream().filter(s -> s != null && !s.isBlank()).map(s -> s + "0").collect(Collectors.toList());

        System.out.println(result);
    }
}
