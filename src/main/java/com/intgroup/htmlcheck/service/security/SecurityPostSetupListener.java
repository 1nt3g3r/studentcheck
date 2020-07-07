package com.intgroup.htmlcheck.service.security;

import com.intgroup.htmlcheck.domain.security.Role;
import com.intgroup.htmlcheck.domain.security.User;
import com.intgroup.htmlcheck.feature.taskcheck.remoteserver.RemoteCheckServers;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStat;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatService;
import com.intgroup.htmlcheck.feature.taskstat.UserTaskStatSpecifications;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUser;
import com.intgroup.htmlcheck.feature.telegram.domain.TelegramUserSpecifications;
import com.intgroup.htmlcheck.feature.telegram.service.TelegramUserService;
import com.intgroup.htmlcheck.feature.telegram.stat.TelegramUserTaskStatService;
import com.intgroup.htmlcheck.repository.RoleRepository;
import com.intgroup.htmlcheck.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SecurityPostSetupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RemoteCheckServers remoteCheckServers;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        addDefaultRolesIfNotExist();
        addDefaultAdminIfNotExists();

        remoteCheckServers.init();

        //checkUsers();
    }

    private void addDefaultRolesIfNotExist() {
        if (roleRepository.findByRole("USER") == null) {
            Role userRole = new Role();
            userRole.setRole("USER");
            userRole.setDescription("Обычный пользователь. Может создавать проекты, и работать с ними");
            roleRepository.save(userRole);
        }

        if (roleRepository.findByRole("ADMIN") == null) {
            Role userRole = new Role();
            userRole.setRole("ADMIN");
            userRole.setDescription("Администратор. Может добавлять роли, создавать и редактировать пользователей");
            roleRepository.save(userRole);
        }
    }

    private void addDefaultAdminIfNotExists() {
        User admin = userRepository.findByEmail("admin");
        if (admin == null) {
            admin = new User();
            admin.setEmail("admin");
            admin.setPassword("fdsa54wy5gfa675hnv");
            admin.setName("Admin");
            admin.setLastName("Admin");

            admin.setPassword(bCryptPasswordEncoder.encode("fdsa54wy5gfa675hnv"));
            admin.setActive(1);
            Role adminRole = roleRepository.findByRole("ADMIN");
            admin.setRoles(new HashSet<Role>(Arrays.asList(adminRole)));
            userRepository.save(admin);
        }
    }

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private UserTaskStatService statService;

    private void checkUsers() {
        List<TelegramUser> users = telegramUserService.queryAll(TelegramUserSpecifications.tagIs("TEEN33"));


        List<String> tasks = Arrays.asList("html-teens-1", "css-teens-2", "js-teens-3", "html-teens-4", "css-teens-5", "js-teens-6",
                "html-teens-7", "css-teens-8", "js-teens-9", "html-teens-10", "css-teens-11", "js-teens-12");

        for(TelegramUser user: users) {
            String emailSuffix = "@telegram-user";
            String email = user.getUserId() + emailSuffix;

            List<UserTaskStat> taskStats = statService.queryAll(UserTaskStatSpecifications.userIdIs(email));

            Set<String> solvedTaskIds = new HashSet<>();
            List<Integer> solvedTasks = new ArrayList<>();
            for(String taskId: tasks) {
                boolean taskFound = false;
                boolean taskSolved = false;
                for(UserTaskStat taskStat: taskStats) {
                    if (taskStat.getTaskId().equals(taskId) ) {
                        taskSolved = taskStat.isSolved();
                        taskFound = true;

                        if (taskSolved) {
                            solvedTaskIds.add(taskId);
                        }
                        break;
                    }
                }

                if (taskFound) {
                    solvedTasks.add(taskSolved ? 1 : 0);
                }
            }

            //Remove last not-fixed tasks
            boolean doIt = true;
            while(doIt) {
                if (solvedTasks.size() <= 0) {
                    doIt = false;
                } else {
                    if (solvedTasks.get(solvedTasks.size() - 1) == 0) {
                        solvedTasks.remove(solvedTasks.size() - 1);
                    } else {
                        doIt = false;
                    }
                }
            }

            int sum = 0;
            for(int solvedTaskItem: solvedTasks) {
                sum += solvedTaskItem;
            }

            if (sum != solvedTasks.size()) {
                System.out.println("Bad user: " + user.getUserId());
            }
        }

        System.out.println("End check");

    }
}
