package com.intgroup.htmlcheck.controller;

import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.service.security.EmailValidateService;
import com.intgroup.htmlcheck.service.security.LoginSuccessRedirectService;
import com.intgroup.htmlcheck.service.security.UserService;
import com.intgroup.htmlcheck.service.util.UserPrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

@Controller
public class SecurityController {
    private static final boolean REGISTRATION_ENABLED = true;

    @Autowired
    private EmailValidateService emailValidateService;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginSuccessRedirectService loginSuccessRedirectService;

    @Autowired
    private UserPrefService userPrefService;

    @GetMapping("/login")
    public String login() {
        return "security/login";
    }

    @GetMapping("/login/success")
    public String loginSuccess(HttpServletRequest request) {
        return loginSuccessRedirectService.getRedirect();
    }

    @GetMapping("/registration")
    public ModelAndView registration(){
        ModelAndView result = new ModelAndView("security/registration");

        addAntibotTask(result);

        return result;
    }

    @PostMapping("/registration")
    public ModelAndView createNewUser(
            @RequestParam String name,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String antibotNumber,
            @RequestParam String antibotAnswer) {
        if (!REGISTRATION_ENABLED) {
            return new ModelAndView("security/no-registration");
        }
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("security/registration");

        addAntibotTask(modelAndView);

        if (userService.findUserByEmail(email) != null) {
            modelAndView.addObject("error", "Пользователь с таким Email уже зарегистрирован");
        } else {
            String correctAntibotAnswer = "ApplesAndBananas";
            if (!antibotAnswer.equals(correctAntibotAnswer)) {
                modelAndView.addObject("error", "Неправильный ответ на антибот задание");
                return modelAndView;
            }

            if (!emailValidateService.isValid(email)) {
                modelAndView.addObject("error", "Введите правильный email");
                return modelAndView;
            }

            if (!isPasswordValid(password)) {
                modelAndView.addObject("error", "Длина пароля должна быть не менее 5 символов");
                return modelAndView;
            }

            if (!isNameValid(name)) {
                modelAndView.addObject("error", "Введите ваше имя");
                return modelAndView;
            }

            if (!isNameValid(lastName)) {
                modelAndView.addObject("error", "Введите фамилию");
                return modelAndView;
            }

            email = email.toLowerCase();

            User user = new User();
            user.setName(name);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setEmail(email);

            userService.saveUser(user);
            modelAndView.addObject("success", "Вы успешно зарегистрировались");
        }

        return modelAndView;
    }


    @GetMapping("/profile")
    public ModelAndView showProfile() {
        ModelAndView result = new ModelAndView("security/profile");

        User user = userService.getUser();
        result.addObject("user", user);

        //Add API key
        result.addObject("apiKey", userPrefService.getApiKey(user));

        return result;
    }

    @PostMapping("/profile")
    public String editProfile(@RequestParam(name = "firstName", required = false) String firstName,
                              @RequestParam(name = "lastName", required = false) String lastName,
                              @RequestParam(name = "password", required = false) String password,

                              @RequestParam(name = UserPrefService.API_KEY) String apiKey) {

        User user = userService.getUser();

        if (user != null) {
            //Save API key
            userPrefService.setApiKey(user, apiKey);
        }


        if (isProfileItemValid(password)) {
            userService.changePassword(user.getId(), password);
            user = userService.getUser();
        }

        if (isProfileItemValid(firstName)) {
            user.setName(firstName);
        }

        if (isProfileItemValid(lastName)) {
            user.setLastName(lastName);
        }

        userService.updateUser(user);

        return "redirect:/profile";
    }

    private boolean isProfileItemValid(String profileItem) {
        return profileItem != null && profileItem.trim().length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    private boolean isNameValid(String name) {
        return name != null && name.trim().length() > 0;
    }

    private void addAntibotTask(ModelAndView modelAndView) {
        int antibotNumber = 500 + new Random().nextInt(1000);
        modelAndView.addObject("antibotNumber", antibotNumber);
    }
}
