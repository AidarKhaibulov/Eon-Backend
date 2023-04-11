package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.EmailDetails;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.services.EmailService;
import rest.eon.services.NotificationService;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final NotificationService notificationService;
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    TimerTask reminder = new TimerTask() {
        public void run() {
            List<Task> l = taskService.getRelevantTasksWithEnabledNotifications();
            l.forEach(t -> {
                LocalDateTime taskTime = LocalDateTime.parse(t.getDateStart(), format);
                LocalDateTime curTime = LocalDateTime.now();
                Long minutes=Long.parseLong(notificationService.getById(t.getNotificationId()).get().getAlarmBefore());
                // checking if task owner should be notified
                if (curTime.plusMinutes(minutes).isEqual(taskTime) ||
                        curTime.plusMinutes(minutes).isAfter(taskTime)) {
                    User u = userService.getUserById(t.getUserId()).get();

                    String message = "Don't forget about your task!\n" + t.getTitle() + " " + t.getDateStart();
                    EmailDetails details = new EmailDetails(u.getEmail(), message, "EON");
                    String status = emailService.sendSimpleMail(details);
                    System.out.println(status);
                }
            });
            System.out.println("Emails send at" + new Date());

        }
    };
    Timer timer = new Timer("Timer");

    long delay = 1000L* 60L * 10L;
    long period = 1000L * 60L * 10L;

    public void taskChecking() {
        timer.scheduleAtFixedRate(reminder, delay, period);
    }
}