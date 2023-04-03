package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;


    @GetMapping()
    public List<Task> fetchTasks() {
        String currentUserEmail = SecurityUtil.getSessionUser();
        User user = userService.getUserByEmail(currentUserEmail).get();
        logger.info("fetched all the tasks");
        List<Task> tasks = new ArrayList<>();
        user.getTasks().forEach(t -> tasks.add(taskService.getTaskById(t).get()));
        return tasks;
    }

    @PostMapping()
    ResponseEntity<Task> createTask(@Valid @RequestBody TaskDto task) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
        Task createdTask = taskService.save(taskService.mapToTask(task));
        if (createdTask != null) return ResponseEntity.ok(createdTask);
        else return null;
    }

    @PutMapping("/{id}")
    ResponseEntity<Task> editTask(@Valid @RequestBody TaskDto newTask, @PathVariable String id) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        return taskService.getTaskById(id).map(task -> {
            task.setDate(newTask.getDate());
            task.setTitle(newTask.getTitle());
            task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
            logger.info("Task with id " + id + " has been updated!");
            return ResponseEntity.ok(taskService.save(task));
        }).orElseGet(() -> ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask))));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteTask(@PathVariable String id) {
        Task taskToDelete = taskService.getTaskById(id).orElse(null);
        if (taskToDelete == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        else {
            taskService.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }

    }

}
