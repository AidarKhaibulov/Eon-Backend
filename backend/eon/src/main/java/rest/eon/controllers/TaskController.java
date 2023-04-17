package rest.eon.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;
import java.util.NoSuchElementException;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Represents api methods for tasks")
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

    @Operation(summary = "Returns all the tasks created by user, including those in groups")
    @GetMapping()
    List<Task> fetchTasks(@RequestBody
                          @Parameter(name = "Sorting parameters", description = "Parameters for sorting tasks")
                          TaskOptions options) {
        List<Task> l = taskService.getTasks(null, options.dateStart, options.dateFinish);
        taskService.sortTasks(l, options.sortingMethod);
        return l;
    }

    @Operation(summary = "Returns all tasks from specified group")
    @GetMapping("/{groupId}")
    List<Task> fetchTasksFromGroup(@RequestBody
                                   @Parameter(name = "Sorting parameters", description = "Parameters for sorting tasks")
                                   TaskOptions options,

                                   @PathVariable
                                   @Parameter(description = "Specified group id")
                                   String groupId) {
        List<Task> l = taskService.getTasks(groupId, options.dateStart, options.dateFinish);
        taskService.sortTasks(l, options.sortingMethod);
        return l;
    }

    @Operation(summary = "Creates new task")
    @PostMapping()
    ResponseEntity<Task> createTask(@Valid
                                    @RequestBody
                                    @Parameter(description = "New task")
                                    TaskDto task) {
        return taskService.addNewTask(task, null);
    }

    @Operation(summary = "Creates new task in specified group")
    @PostMapping("/{groupId}")
    HttpEntity<?> createTaskInGroup(@Valid
                                    @RequestBody
                                    @Parameter(description = "New task")
                                    TaskDto task,

                                    @PathVariable
                                    @Parameter(description = "Specified group")
                                    String groupId) {
        if (userNotGroupAdmin(groupService.getGroupById(groupId).get()))
            return new ResponseEntity<>("Such a group not found", HttpStatus.FORBIDDEN);
        return taskService.addNewTask(task, groupId);
    }

    @Operation(summary = "Finishes provided tasks")
    @PutMapping("/finish/{taskId}")
    ResponseEntity<?> finishTask(@PathVariable String taskId) {
        Task t = taskService.getTaskById(taskId).get();
        String currentUserEmail = SecurityUtil.getSessionUser();
        User u = userService.getUserByEmail(userService.getUserByEmail(currentUserEmail).get().getEmail()).get();

        if (!u.getTasks().contains(taskId)) return TaskNotFound();

        t.setCompleted(true);
        return ResponseEntity.ok(taskService.update(t));
    }


    @Operation(summary = "Updates provided tasks")
    @PutMapping("/{id}")
    ResponseEntity<?> editTask(@Valid
                               @RequestBody
                               @Parameter(description = "New task data without id")
                               TaskDto newTask,

                               @PathVariable
                               @Parameter(description = "Id of edited task")
                               String id) {

        //String ld = String.valueOf((LocalDate.parse(newTask.getDateFinish().substring(0, 10)).getDayOfWeek()));

        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(id))
            return TaskNotFound();
        return taskService.getTaskById(id).map(task -> {
            task.setDateStart(newTask.getDateStart());
            task.setDateFinish(newTask.getDateFinish());
            task.setTitle(newTask.getTitle());
            logger.info("Task with id {0} has been updated!", id);
            return ResponseEntity.ok(taskService.update(task));
        }).orElseGet(() -> ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask))));
    }

    @Operation(summary = "Set notification to provided task")
    @PutMapping("/{taskId}/setNotification")
    ResponseEntity<?> setNotificationToTask(@Valid @RequestBody
                                            NotificationDto newNotification,

                                            @PathVariable @Parameter(description = "Id of task which is set notification")
                                            String taskId) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(taskId))
            return TaskNotFound();

        // evaluating minutes for notification from different date units types
        int minutes;
        switch (newNotification.getUnitsType()) {
            case "days" -> minutes = newNotification.getAlarmBefore() * 24 * 60 * 60;
            case "hours" -> minutes = newNotification.getAlarmBefore() * 60;
            case "minutes" -> minutes = newNotification.getAlarmBefore();
            default -> minutes = 0;
        }
        Notification toSave = Notification.builder()
                .alarmBefore(String.valueOf(minutes))
                .taskId(taskId)
                .build();

        return ResponseEntity.ok(notificationService.save(toSave));
    }

    @Operation(summary = "Set repetition to provided task")
    @PutMapping("/{taskId}/setRepeat")
    ResponseEntity<?> setRepeatForTask(@Valid @RequestBody
                                       List<String> newRepetition,

                                       @PathVariable @Parameter(description = "Id of task which is set repetition")
                                       String taskId) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(taskId))
            return TaskNotFound();

        Repetition toSave = Repetition.builder()
                .repetitionSchema(newRepetition)
                .taskId(taskId)
                .build();
        return ResponseEntity.ok(repetitionService.save(toSave));
    }

    @Operation(summary = "Deletes provided task")
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
    @Schema(name = "Sorting parameters", description = "Parameters for sorting tasks")
    static
    class TaskOptions {
        @Schema(description = """
                default- sort by date in ascending order
                defaultDesc- sort by date in descending order
                day- sort by days in descending order
                dayDesc- sort by days in descending order
                month- sort by months in descending order
                monthDesc- sort by months in descending order
                year- sort by years in descending order
                yearDesc- sort by years in descending order
                name- sort by names in descending order
                nameDesc- sort by names in descending order
                """)
        private String sortingMethod;
        private String dateStart;
        private String dateFinish;
    }
}
