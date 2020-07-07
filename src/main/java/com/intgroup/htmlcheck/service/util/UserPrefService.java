package com.intgroup.htmlcheck.service.util;

import com.intgroup.htmlcheck.domain.UserPreference;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.repository.UserPrefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPrefService {
    @Autowired
    private UserPrefRepository userPrefRepository;

    public static final String API_KEY = "apiKey";

    public List<User> getUsersWithApiKey(String apiKey) {
        return userPrefRepository.getUsersWithOption(API_KEY, apiKey);
    }

    public String getApiKey(User user) {
        return getAsString(user, API_KEY, "");
    }

    public void setApiKey(User user, String apiKey) {
        save(user, API_KEY, apiKey);
    }

    public List<User> getUsersWithOption(String prefName, String prefValue) {
        return userPrefRepository.getUsersWithOption(prefName, prefValue);
    }

    public UserPreference save(User user, String prefName, Object value) {
        UserPreference preference = get(user, prefName);

        if (preference == null) {
            preference = new UserPreference();
        }

        preference.setUser(user);
        preference.setName(prefName);
        preference.setValue(value.toString());

        return userPrefRepository.save(preference);
    }

    public UserPreference get(User user, String prefName) {
        return userPrefRepository.getOption(user.getId(), prefName);
    }

    public String getAsString(User user, String prefName, String defValue) {
        if (user == null) {
            return defValue;
        }

        UserPreference preference = get(user, prefName);

        if (preference == null) {
            return defValue;
        }

        return preference.getValue();
    }

    public boolean getAsBoolean(User user, String prefName, boolean defValue) {
        String boolValue = getAsString(user, prefName, Boolean.toString(defValue));
        return Boolean.parseBoolean(boolValue);
    }

    public int getAsInt(User user, String prefName, int defValue) {
        String value = getAsString(user, prefName, Integer.toString(defValue));
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defValue;
        }
    }
}
