package rest.eon.services.impl;

import org.springframework.stereotype.Service;
import rest.eon.models.Task;
import rest.eon.repositories.TaskRepository;
import rest.eon.services.TaskService;

import java.util.List;
@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }
}
