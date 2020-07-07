package com.intgroup.htmlcheck.feature.block;

import com.google.gson.Gson;
import com.intgroup.htmlcheck.feature.pref.GlobalPrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

@Service
public class MarathonSettingsService {
    public static final String MARATHON_SETTINGS_PREF_NAME = "marathonSettingsPref";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    private GlobalPrefService globalPrefService;

    private Map<String, Map<String, String>> settings;

    @PostConstruct
    public void init() {
        String json = globalPrefService.getOrDefault(MARATHON_SETTINGS_PREF_NAME, "{}");

        try {
            settings = new Gson().fromJson(json, Map.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Map<String, String> getMarathonSettings(LocalDate marathonDate) {
        String key = marathonDate.format(FORMATTER);
        return settings.getOrDefault(key, Collections.emptyMap());
    }

    public String getMarathonSettingValue(LocalDate marathonDate, String settingsName) {
        return getMarathonSettings(marathonDate).getOrDefault(settingsName, "");
    }
}
