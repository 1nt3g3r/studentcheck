package com.intgroup.htmlcheck.feature.shortlink;

import com.google.gson.Gson;
import com.intgroup.htmlcheck.feature.pref.GlobalPrefService;
import org.checkerframework.checker.units.qual.C;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CuttlyService {
    public static final String CUTTLY_API_KEY = "cuttlyApiKey";

    @Autowired
    private GlobalPrefService globalPrefService;

    private Gson gson;
    private String apiKey = "6979effddf50ec6e77e0464e831925a00bc97";

    @PostConstruct
    public void init() {
        gson = new Gson();

        loadSettings();
    }

    public void loadSettings() {
        apiKey = globalPrefService.getOrDefault(CUTTLY_API_KEY, "");
    }

    public String makeShortlink(String url) {
        try {
            Document doc = Jsoup.connect("https://cutt.ly/api/api.php").data("key", apiKey, "short", url).ignoreContentType(true).get();
            CuttlyResponse response = gson.fromJson(doc.body().text(), CuttlyResponse.class);
            if (response.isOk()) {
                return response.getShortlink();
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        CuttlyService cuttlyService = new CuttlyService();
        cuttlyService.init();
        System.out.println(cuttlyService.makeShortlink("https://docs.google.com/spreadsheets/d/1eVeLmlgMMnJWCedCsdVqvCfXfcyh2-4IlAOaM-R015Q/edit?ts=5d6cee6d#gid=1099913410"));
    }
}
