package rest.eon.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.models.Task;
import rest.eon.services.TaskService;

import java.util.List;


@RestController()
@RequestMapping("/tasks")
public class TaskController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/fetchAll")
    public List<Task> fetchTasks() {
        logger.info("fetched all the tasks");
        return taskService.getAll();
    }

    @PostMapping("/add")
    ResponseEntity<Task> createNewTask(@RequestBody Task task) {
        logger.info("Created new task: " + task.toString());
        return ResponseEntity.ok(taskService.save(task));
    }

    @PutMapping("/edit/{id}")
    ResponseEntity<Task> editTask(@RequestBody Task newTask, @PathVariable String id) {
        return taskService.getTaskById(id).map(task -> {
            task.setDate(newTask.getDate());
            task.setTitle(newTask.getTitle());
            logger.info("Task with id " + id + " has been updated!");
            return ResponseEntity.ok(taskService.save(task));
        }).orElseGet(() -> {
            logger.info("Created new task: " + newTask.toString());
            return ResponseEntity.ok(taskService.save(newTask));
        });
    }
    @DeleteMapping("/delete/{id}")
    void deleteTask(@PathVariable String id) {
        logger.info("Task with id "+ id +" has been deleted!");
        taskService.delete(id);
    }

}
