package com.intgroup.htmlcheck.feature.telegram.controller;

import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.block.CheckTaskBlockService;
import com.intgroup.htmlcheck.feature.mq.settings.SettingService;
import com.intgroup.htmlcheck.feature.mq.settings.SettingSpecifications;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserSpecifications;
import com.intgroup.htmlcheck.feature.telegram.im.InstantMessageService;
import com.intgroup.htmlcheck.feature.telegram.im.InstantMessageSpecifications;
import com.intgroup.htmlcheck.feature.telegram.referral.ReferralService;
import com.intgroup.htmlcheck.feature.telegram.service.ImportMetadataService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.PaginationService;
import com.intgroup.htmlcheck.service.util.SeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
@RequestMapping("/admin/tguser")
public class TelegramUserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TelegramUserService telegramUserService;
    
    @Autowired
    private ReferralService referralService;

    @Autowired
    private SeoService seoService;

    @Autowired
    private PaginationService paginationService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private InstantMessageService imService;

    @Autowired
    private ImportMetadataService importMetadataService;

    @GetMapping("/list")
    public ModelAndView list(@RequestParam(name = "currentPage", required = false, defaultValue = "0") int currentPage,
                             @RequestParam(name = "userTag", required = false) String userTag,
                             @RequestParam(name = "query", required = false) String query,
                             @RequestParam(name = "withUnseenMessagesOnly", required = false, defaultValue = "false") boolean withUnseenMessagesOnly,
                             @RequestParam(name = "onlyWhoBlockedBot", required = false, defaultValue = "false") boolean onlyWhoBlockedBot) {
        ModelAndView result = new ModelAndView("admin/tguser/list");

        result.addObject("user", userService.getUser());

        seoService.setTitle(result, "Telegram пользователи");

        List<TelegramUser> users;

        result.addObject("withUnseenMessagesOnly", withUnseenMessagesOnly);
        result.addObject("onlyWhoBlockedBot", onlyWhoBlockedBot);

        //Construct specifications
        List<Specification<TelegramUser>> specs = new ArrayList<>();

        if (onlyWhoBlockedBot) {
            List<Long> userIds = telegramUserService.getUsersThatBlockedBot();
            specs.add(TelegramUserSpecifications.userIdInList(userIds));
        }

        if (withUnseenMessagesOnly) {
            List<Long> userIds = imService.getTelegramUserIdsWithUnseenMessages();
            specs.add(TelegramUserSpecifications.userIdInList(userIds));
        }

        if (userTag != null && !userTag.isBlank() && !userTag.equals("Любой")) {
            result.addObject("userTag", userTag);
            specs.add(TelegramUserSpecifications.tagIs(userTag));
        }

        if (query != null && !query.isBlank()) {
            result.addObject("query", query);
            specs.add(TelegramUserSpecifications.query(query));
        }

        if (specs.isEmpty()) {
            specs.add(TelegramUserSpecifications.any());
        }

        Specification<TelegramUser> userSpec = TelegramUserSpecifications.and(specs);

        int recordCount = telegramUserService.count(userSpec);
        result.addObject("recordCount", recordCount);
        result.addObject("minPage", paginationService.getMinPage(recordCount));
        result.addObject("maxPage", paginationService.getMaxPage(recordCount));
        result.addObject("currentPage", currentPage);
        result.addObject("pageCount", paginationService.getPageCount(recordCount));

        //Clamp current page
        currentPage = Math.min(currentPage, paginationService.getMaxPage(recordCount));

        users = telegramUserService.queryPage(currentPage, userSpec);
        result.addObject("users", users);

        //Add unseen messages and user progress
        List<Long> userIds = new ArrayList<>();
        users.forEach(user -> userIds.add(user.getUserId()));
        result.addObject("unseenMessageCount", imService.getUnseenMessageCountForUsers(userIds));

        //Add unique user tags
        result.addObject("userTags", telegramUserService.getUniqueTags());

        //Mark users that blocked bot
        Set<Long> usersThatBlockedBot = new HashSet<>();
        users.forEach(user -> {
            long userId = user.getUserId();
            if (telegramUserService.isUserBlockedBot(userId)) {
                usersThatBlockedBot.add(userId);
            }
        });
        result.addObject("usersThatBlockedBot", usersThatBlockedBot);

        return result;
    }

    @GetMapping("/delete")
    public String delete(long userId) {
        telegramUserService.deleteById(userId);

        return "redirect:/admin/tguser/list";
    }

    @PostMapping("/sendMessage")
    @ResponseBody
    public String sendMessage(@RequestParam long userId,
                              @RequestParam String message) {
        telegramUserService.queryAll(TelegramUserSpecifications.userIdIs(userId)).forEach(user ->
            telegramBotService.sendSimpleTextMessage(user, message)
        );

        return "ok";
    }

    @GetMapping("/create-update")
    public ModelAndView createUpdate(@RequestParam(required = false, defaultValue = "-1") long userId) {
        ModelAndView result = new ModelAndView("admin/tguser/create-update");

        TelegramUser telegramUser = telegramUserService.getById(userId);
        if (telegramUser == null) {
            telegramUser = new TelegramUser();
            telegramUser.setUserId(userId);
        }
        result.addObject("telegramUser", telegramUser);
        
        result.addObject("invitedUsers", referralService.getInvitedUsers(telegramUser));
        result.addObject("whoInvited", referralService.getWhoInvited(telegramUser));
        
        result.addObject("user", userService.getUser());

        return result;
    }

    @PostMapping("/create-update")
    public String postCreateUpdate(@RequestParam(required = false, defaultValue = "-1") long userId,
                                   @RequestParam String firstName,
                                   @RequestParam String lastName,
                                   @RequestParam String tag,
                                   @RequestParam String eventDate,
                                   @RequestParam String email,
                                   @RequestParam String phone,
                                   @RequestParam String languageCode,
                                   @RequestParam String role,
                                   @RequestParam String metadata
                                   ) {
        TelegramUser telegramUser = null;
        if (userId < 0) {
            telegramUser = new TelegramUser();
        } else {
            telegramUser = telegramUserService.getById(userId);
        }

        telegramUser.setFirstName(firstName);
        telegramUser.setLastName(lastName);
        telegramUser.setTag(tag);
        telegramUser.setEventDate(eventDate);
        telegramUser.setEmail(email);
        telegramUser.setPhone(phone);
        telegramUser.setLanguageCode(languageCode);
        telegramUser.setRole(role);
        telegramUser.setMetadata(metadata.replace("\r", ""));
        telegramUserService.save(telegramUser);

        return "redirect:/admin/tguser/create-update?userId=" + userId;
    }

    @GetMapping("/importMetadata")
    public ModelAndView getImportMetadata() {
        ModelAndView result = new ModelAndView("admin/tguser/metadata/import");

        seoService.setTitle(result, "Импорт метаданных для юзеров");
        result.addObject("user", userService.getUser());

        return result;
    }

    @PostMapping("/importMetadata")
    public ModelAndView postImportMetadata(@RequestParam String phones,
            @RequestParam String metadata) {
        ModelAndView result = new ModelAndView("admin/tguser/metadata/import");

        seoService.setTitle(result, "Импорт метаданных для юзеров");
        result.addObject("user", userService.getUser());

        result.addObject("phones", phones);
        result.addObject("metadata", metadata);

        result.addObject("importMetadataResult",
                importMetadataService.importMetadata(Arrays.asList(phones.replace("\r", "").split("\n")),
                        new LinkedHashSet<>(Arrays.asList(metadata.replace("\r", "").split("\n")))));

        return result;
    }
}
