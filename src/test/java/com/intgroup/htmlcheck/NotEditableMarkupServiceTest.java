package com.intgroup.htmlcheck;

import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format.NotEditableMarkupService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class NotEditableMarkupServiceTest {
    @Autowired
    private NotEditableMarkupService service;

    @Test
    public void testEmptyText() {
        String text = "Text";
        test(text);
    }

    @Test
    public void testSingleLineText() {
        test("<not-edit>Text</not-edit>",
                new int[] {
                        0,0, 0,4
                });
    }

    @Test
    public void testNotClosedTagText() {
        test("<not-edit>Text",
                new int[] {
                        0,0, 0,4
                });
    }

    @Test
    public void testSingleLineText2() {
        test("<not-edit>Text</not-edit>Another",
                new int[] {
                        0,0, 0,4
                });
    }

    @Test
    public void testSingleLineText3() {
        test("<not-edit>Text</not-edit><not-edit>Text2</not-edit>",
                new int[] {
                        0,0, 0,4
                },
                new int[] {
                        0,4, 0,9
                });
    }

    @Test
    public void testMultiLineText() {
        test("<not-edit>Text</not-edit>Another\n" +
                "Some text\n" +
                "Text<not-edit>html</not-edit>void",
                new int[] {
                        0,0, 0,4
                },
                new int[] {
                        2,4, 2,8
                });
    }

    @Test
    public void testCleanText() {
        String sourceText = "<not-edit>Text</not-edit>";
        String expected = "Text";
        String actual = service.cleanText(sourceText);

        Assertions.assertEquals(expected, actual);
    }

    public void test(String text, int[] ... expected) {
        List<NotEditableMarkupService.NotEditableBlock> actual = service.makeNotEditableBlocks(text);
        actual.forEach(i -> System.out.println(i));

        Assertions.assertEquals(expected.length, actual.size());

        for(int i = 0; i < expected.length; i++) {
            Assertions.assertTrue(actual.get(i).is(expected[i]), "Failed at " + actual.get(i));
        }
    }
}
