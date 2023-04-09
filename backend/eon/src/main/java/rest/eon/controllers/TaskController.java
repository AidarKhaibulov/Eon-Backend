package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.services.GroupService;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;
    final private GroupService groupService;

    private static ResponseEntity<String> NotFoundEntity() {
        return new ResponseEntity<>("Such a task not found", HttpStatus.FORBIDDEN);
    }

    /**
     * @return all tasks created by user, including those in groups
     */
    @GetMapping()
    List<Task> fetchTasks(@RequestBody TaskOptions options) {
        List<Task> l=taskService.getTasks(null, options.dateStart, options.dateFinish);
        taskService.sortTasks(l,options.sortingMethod);
        return l;
    }

    /**
     * @param group_id from where to fetch tasks
     * @return all tasks from specified group
     */
    @GetMapping("/{group_id}")
    List<Task> fetchTasksFromGroup(@RequestBody TaskOptions options, @PathVariable String group_id) {
        List<Task> l = taskService.getTasks(group_id, options.dateStart, options.dateFinish);
        taskService.sortTasks(l,options.sortingMethod);
        return l;
    }

    @PostMapping()
    ResponseEntity<Task> createTask(@Valid @RequestBody TaskDto task) {
        return taskService.addNewTask(task, null);
    }

    @PostMapping("/{group_id}")
    ResponseEntity<?> createTaskInGroup(@Valid @RequestBody TaskDto task, @PathVariable String group_id) {
        if (userNotGroupAdmin(groupService.getGroupById(group_id).get()))
            return new ResponseEntity<>("Such a group not found", HttpStatus.FORBIDDEN);
        return taskService.addNewTask(task, group_id);
    }

    @PutMapping("/finish/{task_id}")
    ResponseEntity<?> finishTask(@PathVariable String task_id) {
        Task t = taskService.getTaskById(task_id).get();
        String currentUserEmail = SecurityUtil.getSessionUser();
        User u = userService.getUserByEmail(userService.getUserByEmail(currentUserEmail).get().getEmail()).get();

        if (!u.getTasks().contains(task_id)) return NotFoundEntity();

        t.setCompleted(true);
        return ResponseEntity.ok(taskService.update(t));
    }

    @PutMapping("/{id}")
    ResponseEntity<?> editTask(@Valid @RequestBody TaskDto newTask, @PathVariable String id) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        if (!userService.getUserByEmail(currentUserEmail).get().getTasks().contains(id))
            return NotFoundEntity();
        return taskService.getTaskById(id).map(task -> {
            task.setDateStart(newTask.getDateStart());
            task.setDateFinish(newTask.getDateFinish());
            task.setTitle(newTask.getTitle());
            logger.info("Task with id " + id + " has been updated!");
            return ResponseEntity.ok(taskService.update(task));
        }).orElseGet(() -> ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask))));
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
