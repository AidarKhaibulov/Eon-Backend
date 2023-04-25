package rest.eon.services;

import org.springframework.http.ResponseEntity;
import rest.eon.controllers.TaskController;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;

import java.io.InvalidObjectException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskService{
    List<Task> getAll();
    List<Task> getRelevantTasksWithEnabledNotifications();
    Task save(Task task) throws InvalidObjectException;

    Optional<Task> getTaskById(String id);

    void delete(String id);

    Task mapToTask(TaskDto taskDto);


    Task update(Task task);


    List<Task> getTasks(String group_id, String start, String finish);

    void sortTasks(List<Task> l, String method);

    ResponseEntity<Task> addNewTask(TaskDto task, String group_id) throws Exception;
}
