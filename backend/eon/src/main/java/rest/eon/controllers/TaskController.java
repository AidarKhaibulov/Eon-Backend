package rest.eon.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;
import rest.eon.services.TaskService;
import rest.eon.services.impl.UserService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
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

    @GetMapping()
    public List<Task> fetchTasks() {
        logger.info("fetched all the tasks");
        return taskService.getAll();
    }

    @PostMapping()
    ResponseEntity<Task> createNewTask(@Valid @RequestBody TaskDto task) {
        String currentUserEmail= SecurityUtil.getSessionUser();
        task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
        Task createdTask=taskService.save(taskService.mapToTask(task));
        if(createdTask!=null)
        return ResponseEntity.ok(createdTask);
        else return null;
    }

    @PutMapping("/{id}")
    ResponseEntity<Task> editTask(@Valid @RequestBody TaskDto newTask, @PathVariable String id) {
        String currentUserEmail= SecurityUtil.getSessionUser();
        return taskService.getTaskById(id).map(task -> {
            task.setDate(newTask.getDate());
            task.setTitle(newTask.getTitle());
            task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
            logger.info("Task with id " + id + " has been updated!");
            return ResponseEntity.ok(taskService.save(task));
        }).orElseGet(() -> ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask))));
    }

    @DeleteMapping("/{id}")
    void deleteTask(@PathVariable String id) {
        taskService.delete(id);
    }

}
