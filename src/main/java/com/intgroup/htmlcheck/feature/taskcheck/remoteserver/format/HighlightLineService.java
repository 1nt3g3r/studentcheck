package com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HighlightLineService {
    public static final String HL_TAG = "<hl-line>";

    public String clean(String text) {
        return text.replace(HL_TAG, "");
    }

    public List<HlLine> getHlLines(String text) {
        List<HlLine> result = new ArrayList<>();

        String[] lines = text.split("\n");
        for(int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(HL_TAG)) {
                result.add(HlLine.create(i));
            }
        }

        return result;
    }

    @Data
    public static class HlLine {
        private int line;
        private String className = "hl-mark-line";

        public static HlLine create(int line) {
            HlLine result = new HlLine();
            result.setLine(line);
            return result;
        }
    }
}
