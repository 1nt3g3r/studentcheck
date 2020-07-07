package com.intgroup.htmlcheck.feature.pref;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalPrefService {
    @Autowired
    private GlobalPrefRepository prefRepository;

    public String getOrDefault(String name, String value) {
        if (prefRepository.existsById(name)) {
            return prefRepository.getOne(name).getValue();
        }

        return value;
    }

    public boolean getOrDefaultBool(String name, boolean defValue) {
        return Boolean.parseBoolean(getOrDefault(name, defValue + ""));
    }

    public int getOrDefaultInt(String name, int defValue) {
        return Integer.parseInt(getOrDefault(name, defValue + ""));
    }

    public void save(String name, Object value) {
        GlobalPref pref = null;
        if (prefRepository.existsById(name)) {
            pref = prefRepository.getOne(name);
        } else {
            pref = new GlobalPref();
            pref.setName(name);
        }

        pref.setValue(value.toString());

        prefRepository.save(pref);
    }
}
