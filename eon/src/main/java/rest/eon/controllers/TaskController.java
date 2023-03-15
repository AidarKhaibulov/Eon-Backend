package rest.eon.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rest.eon.models.Task;
import rest.eon.services.TaskService;

import java.util.List;

@RestController
public class TaskController {
    final private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks/get")
    public List<Task> fetchTasks() {
        return taskService.getAllTasks();
    }


}
