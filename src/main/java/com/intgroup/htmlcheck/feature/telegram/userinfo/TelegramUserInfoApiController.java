package com.intgroup.htmlcheck.feature.telegram.userinfo;

import com.intgroup.htmlcheck.controller.api.RequestResult;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.bgtask.BgTaskService;
import com.intgroup.htmlcheck.feature.certificate.CreateCertificateImageService;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotPayloadParseService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramBotService;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.NormalizeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;

@RestController
@RequestMapping("/api/v2/tguser")
public class TelegramUserInfoApiController {
    @Autowired
    private TelegramBotPayloadParseService payloadParseService;

    @Autowired
    private TelegramUserInfoService telegramUserInfoService;

    @Autowired
    private UserService userService;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private BgTaskService bgTaskService;

    @Autowired
    private NormalizeDataService normalizeDataService;

    @RequestMapping(value = "/setInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public Object setInfo(@RequestParam String phone,
                          @RequestParam(name = "firstName", required = false) String firstName,
                          @RequestParam(name = "email", required = false) String email,
                          @RequestParam(name = "metadata", required = false) String metadata) {
        phone = normalizeDataService.normalizePhone(phone);

        telegramUserInfoService.setInfo(phone, firstName, email, metadata);

        return RequestResult.success("ok");
    }

    @RequestMapping(value = "/requestResumeCertificate", method = {RequestMethod.GET, RequestMethod.POST})
    public Object requestResumeCertificate(@RequestParam(name = "token", required = false) String token,
                                           @RequestParam(name = "firstName", required = false) String firstName,
                                           @RequestParam(name = "lastName", required = false) String lastName,
                                           @RequestParam(name = "phone", required = false) String phone) {

        User user = userService.findUserByToken(token);

        if (user == null) {
            return RequestResult.fail("Invalid token");
        }

        String email = user.getEmail();
        String telegramUserIdString = email.split("@")[0];

        long telegramUserId = 0;
        try {
            telegramUserId = Long.parseLong(telegramUserIdString);
        } catch (Exception ex) {
            ex.printStackTrace();

            return RequestResult.fail("Пользователь не зарегистрирован в Телеграм боте");
        }

        TelegramUser telegramUser = telegramUserService.getById(telegramUserId);
        if (telegramUser == null) {
            return RequestResult.fail("Пользователь не найден");
        }

        if (firstName != null && !firstName.isBlank()) {
            telegramUser.setFirstName(firstName);
        }

        if (lastName != null && lastName.isBlank()) {
            telegramUser.setLastName(lastName);
        }

        if (phone == null || phone.length() < 10) {
            return RequestResult.fail("Введите телефон (минимум 10 цифр)");
        }

        telegramUser.setPhone(phone);

        telegramUserService.save(telegramUser);

        //Send sertificate
        telegramBotService.sendSimpleTextMessage(telegramUser, "Крутая работа! Вот ваш сертификат:");
        try {
            sendCertificate(telegramUser, firstName, lastName);
        } catch (IOException e) {
            return RequestResult.fail("Что-то пошло не так");
        }

        return RequestResult.success("Спасибо, ожидайте сертификат");
    }

    public void sendCertificate(TelegramUser telegramUser, String firstName, String lastName) throws IOException {
        StringJoiner fullName = new StringJoiner(" ");
        if (firstName != null && !firstName.isBlank()) {
            fullName.add(firstName);
        }

        if (lastName != null && !lastName.isBlank()) {
            fullName.add(lastName);
        }

        //Make certificate
        BufferedImage certificateImage = CreateCertificateImageService.makeCertificate(fullName.toString());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(certificateImage, "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        SendDocument certificateDoc = new SendDocument();
        certificateDoc.setDocument("certificate.png", is);

        telegramBotService.send(telegramUser, certificateDoc);

        //Close streams
        bgTaskService.submitTask("Close IO after certificate send", 1, () -> {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, true);
    }
}
