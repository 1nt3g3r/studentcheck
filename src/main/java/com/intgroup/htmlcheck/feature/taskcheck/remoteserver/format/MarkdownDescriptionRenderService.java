package com.intgroup.htmlcheck.feature.taskcheck.remoteserver.format;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class MarkdownDescriptionRenderService {
    @Value("${domainHost}")
    private String domainHost;

    private HtmlRenderer htmlRenderer;
    private List<Extension> commonMarkExtensions = Arrays.asList(TablesExtension.create());

    @PostConstruct
    public void init() {
        setupHtmlRenderer();

        if (domainHost == null) {
            domainHost = "http://localhost:8888";
        }
    }

    private void setupHtmlRenderer() {
        htmlRenderer = HtmlRenderer
                .builder()
                .extensions(commonMarkExtensions)
                .build();
    }

    public String renderHtml(String markdown, TextProcessor ... postProcessors) {
        if (markdown == null) {
            return "";
        }
        Parser parser = Parser.builder().extensions(commonMarkExtensions).build();
        Node document = parser.parse(markdown);
        String html = htmlRenderer.render(document);

        for(TextProcessor processor: postProcessors) {
            html = processor.process(html);
        }

        html = html.replace("${domainHost}", domainHost);

        return html;
    }

    public interface TextProcessor {
        String process(String html);
    }

    public static void main(String[] args) {
        MarkdownDescriptionRenderService mdService = new MarkdownDescriptionRenderService();
        mdService.init();

        String markdown = "Переменные - это основа в программировании.\n" +
                "\n" +
                "Вот иллюстрация, поясняющая сложность:\n" +
                "\n" +
                "![](let.png)";

        String html = mdService.renderHtml(markdown, text -> {
            String pattern = "<img src=\"";
            String replace = pattern + "${domainHost}/task-cache/js/";
            return text.replace(pattern, replace);
        });

        System.out.println(html);
    }
}
