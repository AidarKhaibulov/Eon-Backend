package rest.eon.services;

import org.springframework.stereotype.Service;
import rest.eon.models.Task;

import java.util.List;
public interface TaskService{
    List<Task> getAllTasks();
}
