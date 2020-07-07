package com.intgroup.htmlcheck.feature.doc;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DocService {
    public String getDoc(String name) {
        String fullPath = "./data/docs/" + name + ".md";

        if (!new File(fullPath).exists()) {
            return "";
        }

        try {
            String markdown = Files.readString(Path.of(fullPath), StandardCharsets.UTF_8);
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdown);
            return HtmlRenderer.builder().build().render(document);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
