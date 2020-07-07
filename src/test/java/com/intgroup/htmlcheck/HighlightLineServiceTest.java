package com.intgroup.htmlcheck;

import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.HighlightLineService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HighlightLineServiceTest {
    @Autowired
    private HighlightLineService service;

    @Test
    public void testCleanText() {
        String text = "<hl-line>Line 1\n" +
                "Line 2";

        String actualText = service.clean(text);
        String expectedText = "Line 1\n" +
                "Line 2";

        Assertions.assertEquals(expectedText, actualText);
    }

    @Test
    public void testSingleLineText() {
        String text = "<hl-line>Line 1";

        List<HighlightLineService.HlLine> result = service.getHlLines(text);
        Assertions.assertEquals(1, result.size());

        HighlightLineService.HlLine hlLine = result.get(0);
        Assertions.assertEquals(0, hlLine.getLine());
    }

    @Test
    public void testMultiLineText() {
        String text = "<hl-line>Line 1\n" +
                "Line 2\n" +
                "<hl-line>Line 3";

        List<HighlightLineService.HlLine> result = service.getHlLines(text);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals(0, result.get(0).getLine());
        Assertions.assertEquals(2, result.get(1).getLine());
    }
}
