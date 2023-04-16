package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.NotificationDto;
import rest.eon.dto.TaskDto;
import rest.eon.models.*;
import rest.eon.services.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;
    final private GroupService groupService;
    final private NotificationService notificationService;
    final private RepetitionService repetitionService;

    private static ResponseEntity<String> TaskNotFound() {
        return new ResponseEntity<>("Such a task not found", HttpStatus.FORBIDDEN);
    }

    /**
     * @return all tasks created by user, including those in groups
     */
    @GetMapping()
    List<Task> fetchTasks(@RequestBody TaskOptions options) {
        List<Task> l = taskService.getTasks(null, options.dateStart, options.dateFinish);
        taskService.sortTasks(l, options.sortingMethod);
        return l;
    }

    /**
     * @param groupId from where to fetch tasks
     * @return all tasks from specified group
     */
    @GetMapping("/{groupId}")
    List<Task> fetchTasksFromGroup(@RequestBody TaskOptions options, @PathVariable String groupId) {
        List<Task> l = taskService.getTasks(groupId, options.dateStart, options.dateFinish);
        taskService.sortTasks(l, options.sortingMethod);
        return l;
    }

    @PostMapping()

    ResponseEntity<Task> createTask(@Valid @RequestBody TaskDto task) {
        return taskService.addNewTask(task, null);
    }

    @PostMapping("/{groupId}")
    HttpEntity<?> createTaskInGroup(@Valid @RequestBody TaskDto task, @PathVariable String groupId) {
        if (userNotGroupAdmin(groupService.getGroupById(groupId).get()))
            return new ResponseEntity<>("Such a group not found", HttpStatus.FORBIDDEN);
        return taskService.addNewTask(task, groupId);
    }

    @PutMapping("/finish/{taskId}")
    ResponseEntity<?> finishTask(@PathVariable String taskId) {
        Task t = taskService.getTaskById(taskId).get();
        String currentUserEmail = SecurityUtil.getSessionUser();
        User u = userService.getUserByEmail(userService.getUserByEmail(currentUserEmail).get().getEmail()).get();

        if (!u.getTasks().contains(taskId)) return TaskNotFound();

        t.setCompleted(true);
        return ResponseEntity.ok(taskService.update(t));
    }


    @PutMapping("/{id}")
    ResponseEntity<?> editTask(@Valid @RequestBody TaskDto newTask, @PathVariable String id) {

        //String ld = String.valueOf((LocalDate.parse(newTask.getDateFinish().substring(0, 10)).getDayOfWeek()));

        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(id))
            return TaskNotFound();
        return taskService.getTaskById(id).map(task -> {
            task.setDateStart(newTask.getDateStart());
            task.setDateFinish(newTask.getDateFinish());
            task.setTitle(newTask.getTitle());
            logger.info("Task with id {0} has been updated!",id);
            return ResponseEntity.ok(taskService.update(task));
        }).orElseGet(() -> ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask))));
    }

    @PutMapping("/{taskId}/setNotification")
    ResponseEntity<?> setNotificationToTask(@Valid @RequestBody NotificationDto newNotification, @PathVariable String taskId) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(taskId))
            return TaskNotFound();

        // evaluating minutes for notification from different date units types
        int minutes;
        switch (newNotification.getUnitsType()) {
            case "days" -> minutes = newNotification.getAlarmBefore() * 24 * 60 * 60;
            case "hours" -> minutes = newNotification.getAlarmBefore() * 60;
            case "minutes" -> minutes = newNotification.getAlarmBefore();
            default -> minutes=0;
        }
        Notification toSave = Notification.builder()
                .alarmBefore(String.valueOf(minutes))
                .taskId(taskId)
                .build();

        return ResponseEntity.ok(notificationService.save(toSave));
    }


    @PutMapping("/{taskId}/setRepeat")
    ResponseEntity<?> setRepeatForTask(@Valid @RequestBody List<String> newRepetition, @PathVariable String taskId) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(taskId))
            return TaskNotFound();

        Repetition toSave = Repetition.builder()
                .repetitionSchema(newRepetition)
                .taskId(taskId)
                .build();
        return ResponseEntity.ok(repetitionService.save(toSave));
    }


    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteTask(@PathVariable String id) {
        try {
            Task taskToDelete = taskService.getTaskById(id).orElse(null);
            if (taskToDelete == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else {
                taskService.delete(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    private boolean userNotGroupAdmin(Group currentGroup) {
        return !currentGroup.getAdmins().contains(userService.getUserByEmail(SecurityUtil.getSessionUser()).get().getId());
    }

    @Data
    @AllArgsConstructor
    static
    class TaskOptions {
        private String sortingMethod;
        private String dateStart;
        private String dateFinish;
    }
}
