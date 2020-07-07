package com.intgroup.htmlcheck.service.util;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class UrlService {
    public String decode(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    public String normalizeURL(String url) {
        url = decode(url);

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        return url;
    }

    public String addDomainIfNeeded(String url, String host) {
        url = decode(url);
        if (!url.contains(host)) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            url = host + url;
        }
        String normalized = normalizeURL(url);
        return normalized;
    }

    public String parseProtocol(String url) {
        String delimiter = "://";

        String[] parts = url.split(delimiter);
        if (parts.length > 1) {
            return parts[0] + delimiter;
        } else {
            return "";
        }
    }

    public String parseDomain(String url) {
        int start = url.indexOf("//", 0);
        int end = url.indexOf("/", start + 2);

        if (start < 0) {
            start = 0;
        } else {
            start += 2;
        }

        if (end < 0) {
            end = url.length();
        }

        String result = url.substring(start, end);
        if (result.startsWith("www.")) {
            result = result.substring(4, result.length());
        }

        return result.toLowerCase();
    }

    public List<String> getUniqueDomains(List<String> urls) {
        List<String> result = new ArrayList<>();
        urls.forEach(url -> {
            String domain = parseDomain(url);
            if (!result.contains(domain)) {
                result.add(domain);
            }
        });
        return result;
    }
}
