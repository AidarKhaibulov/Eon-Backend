package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.EmailDetails;
import rest.eon.auth.SecurityUtil;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.repositories.TaskRepository;
import rest.eon.services.EmailService;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service for checking tasks with enabled notifications to send email
 */
@Service
@RequiredArgsConstructor
public class UserNotificationService {
    private final TaskService taskService;
    private final UserService userService;
    private final EmailService emailService;
    TimerTask userNotification = new TimerTask() {
        public void run() {
            List<Task> l = taskService.getRelevantTasksWithEnabledNotifications();
            l.forEach(t -> {
                User u=userService.getUserById(t.getUserId()).get();
                String message="Don't forget about your task!\n"+t.getTitle()+" "+t.getDateStart();
                EmailDetails details=new EmailDetails(u.getEmail(),message,"EON");
                String status
                        = emailService.sendSimpleMail(details);
                System.out.println(status);
            });
            System.out.println("Email send at" + new Date());

        }
    };
    Timer timer = new Timer("Timer");

    long delay = 1000L;
    long period = 1000L * 5L;
    public void taskChecking(){
        timer.scheduleAtFixedRate(userNotification,delay,period);
    }
}