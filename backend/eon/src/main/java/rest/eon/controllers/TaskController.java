package rest.eon.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.services.TaskService;
import rest.eon.services.impl.UserService;

import java.util.List;
import java.util.Optional;


@RestController()
@RequestMapping("/tasks")
public class TaskController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/fetchAll")
    public List<Task> fetchTasks() {
        logger.info("fetched all the tasks");
        return taskService.getAll();
    }

    @PostMapping("/add")
    ResponseEntity<Task> createNewTask(@Valid @RequestBody TaskDto task) {
        String currentUserEmail= SecurityUtil.getSessionUser();
        task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
        logger.info("Created new task: " + task);
        return ResponseEntity.ok(taskService.save(taskService.mapToTask(task)));
    }

    @PutMapping("/edit/{id}")
    ResponseEntity<Task> editTask(@Valid @RequestBody TaskDto newTask, @PathVariable String id) {
        String currentUserEmail= SecurityUtil.getSessionUser();
        return taskService.getTaskById(id).map(task -> {
            task.setDate(newTask.getDate());
            task.setTitle(newTask.getTitle());
            task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
            logger.info("Task with id " + id + " has been updated!");
            return ResponseEntity.ok(taskService.save(task));
        }).orElseGet(() -> {
            logger.info("Created new task: " + newTask.toString());
            return ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask)));
        });
    }

    @DeleteMapping("/delete/{id}")
    void deleteTask(@PathVariable String id) {
        logger.info("Task with id " + id + " has been deleted!");
        taskService.delete(id);
    }

}
