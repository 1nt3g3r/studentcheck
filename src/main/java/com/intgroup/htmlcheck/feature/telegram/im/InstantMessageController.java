package com.intgroup.htmlcheck.feature.telegram.im;

import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/admin/tguser/im")
public class InstantMessageController {
    @Autowired
    private UserService userService;

    @Autowired
    private InstantMessageService imService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private TelegramBotService telegramBotService;

    @GetMapping
    public ModelAndView im(@RequestParam long telegramUserId) {
        ModelAndView result = new ModelAndView("admin/tguser/im");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "IM");

        result.addObject("telegramUser", telegramUserService.getById(telegramUserId));

        //Mark all unseen as seen
        imService.queryAll(InstantMessageSpecifications.and(
                InstantMessageSpecifications.telegramUserIdIs(telegramUserId),
                InstantMessageSpecifications.messageTypeIs(InstantMessageType.fromUser),
                InstantMessageSpecifications.seen(false)
        )).forEach(instantMessage -> {
            instantMessage.setSeen(true);
            imService.save(instantMessage);
        });

        List<InstantMessage> messages = imService.queryAll(InstantMessageSpecifications.telegramUserIdIs(telegramUserId));
        result.addObject("messages", messages);

        return result;
    }

    @PostMapping("/sendMessage")
    public String sendMessage(@RequestParam long telegramUserId,
                              @RequestParam String message) {

        telegramBotService.sendSimpleTextMessage(telegramUserService.getById(telegramUserId), message);

        return "redirect:/admin/tguser/im?telegramUserId=" + telegramUserId;
    }
}
