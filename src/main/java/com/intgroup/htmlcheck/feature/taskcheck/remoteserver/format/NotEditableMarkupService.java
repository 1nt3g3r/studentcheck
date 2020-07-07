package com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotEditableMarkupService {
    public static final String START_TAG = "<not-edit>";
    public static final String END_TAG = "</not-edit>";

    public String cleanText(String sourceText) {
        return sourceText.replace(START_TAG, "").replace(END_TAG, "");
    }

    @Data
    public static class NotEditableBlockPart {
        private int line;
        private int ch;
    }

    @Data
    public static class NotEditableBlock {
        private NotEditableBlockPart start = new NotEditableBlockPart();
        private NotEditableBlockPart end = new NotEditableBlockPart();
        private String className = "not-editable-content";

        public static NotEditableBlock create(int startLine, int startChar, int endLine, int endChar) {
            NotEditableBlock result = new NotEditableBlock();
            result.start.line = startLine;
            result.start.ch = startChar;

            result.end.line = endLine;
            result.end.ch = endChar;

            return result;
        }

        @Override
        public String toString() {
            return "From " + start.line + ":" + start.ch + " to " + end.line + ":" + end.ch;
        }

        public boolean is(int[] data) {
            return is(data[0], data[1], data[2], data[3]);
        }

        public boolean is(int startLine, int startChar, int endLine, int endChar) {
            return start.line == startLine &&
                    start.ch == startChar &&
                    end.line == endLine &&
                    end.ch == endChar;
        }
    }

    @Data
    public static class TextBlock {
        private String text;
        private boolean editable;

        public static TextBlock editable(String text) {
            TextBlock result = new TextBlock();
            result.text = text;
            result.editable = true;
            return result;
        }

        public static TextBlock notEditable(String text) {
            TextBlock result = new TextBlock();
            result.text = text;
            result.editable = false;
            return result;
        }
    }

    public List<NotEditableBlock> makeNotEditableBlocks(String text) {
        Map<Integer, List<TextBlock>> textBlocks = new LinkedHashMap<>();

        String[] lines = text.split("\n");
        for(int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];

            List<TextBlock> lineBlocks = new ArrayList<>();
            textBlocks.put(lineIndex, lineBlocks);

            if (line.contains(START_TAG)) {

                String[] lineParts = line.split(START_TAG);

                for(String linePart: lineParts) {
                    //Skip empty lines
                    if (linePart.isEmpty()) {
                        continue;
                    }

                    if (linePart.contains(END_TAG)) {
                        String[] endTagParts = linePart.split(END_TAG);

                        lineBlocks.add(TextBlock.notEditable(endTagParts[0]));
                    } else {
                        if (line.startsWith(START_TAG)) {
                            lineBlocks.add(TextBlock.notEditable(linePart));
                        } else {
                            lineBlocks.add(TextBlock.editable(linePart));
                        }
                    }
                }
            }
        }

        List<NotEditableBlock> result = new ArrayList<>();

        textBlocks.keySet().forEach(lineIndex -> {
            int totalLength = 0;
            List<TextBlock> lineBlocks = textBlocks.get(lineIndex);
            for(TextBlock lineBlock: lineBlocks) {
                if (!lineBlock.isEditable()) {
                    int endIndex = totalLength + lineBlock.text.length();

                    result.add(NotEditableBlock.create(lineIndex, totalLength, lineIndex, endIndex));
                }

                totalLength += lineBlock.text.length();
            }
        });

        return result;
    }
}
