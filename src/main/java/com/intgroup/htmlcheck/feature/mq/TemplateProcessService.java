package com.intgroup.htmlcheck.feature.mq;

import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Service
public class TemplateProcessService {
    private Configuration cfg;
    private Map<String, Template> templateCache;

    @PostConstruct
    public void init() {
      initConfig();

      templateCache = new HashMap<>();
    }

    private void initConfig() {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
    }

    public String process(String template, Object ... keyValues) {
        Map<String, Object> data = new HashMap<>();
        for(int i = 0; i < keyValues.length; i += 2) {
            String key = keyValues[i].toString();
            Object value = keyValues[i+1];

            data.put(key, value);
        }

        return process(template, data);
    }

    public String process(String templateText, Map<String, Object> data) {
        try {
            Template template = templateCache.getOrDefault(templateText, null);
            if (template == null) {
                template = new Template("message", new StringReader(templateText), cfg);
                templateCache.put(templateText, template);
            }

            Writer out = new StringWriter();
            template.process(data, out);

            return out.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public static void main(String[] args) throws IOException, TemplateException {
        TemplateProcessService service = new TemplateProcessService();
        service.init();

        TelegramUser user = new TelegramUser();
        user.setFirstName("Ivan");
        user.setEmail("melnichuk");

        long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++) {
            // Get your template as a String from the DB
            String template = "Hello ${user.firstName}\n" +
                    "<#if user.email=\"melnichuk\">Показываем текст</#if>";

            service.process(template, "user", user);
            //System.out.println(service.process(template, "user", user));
        }

        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
