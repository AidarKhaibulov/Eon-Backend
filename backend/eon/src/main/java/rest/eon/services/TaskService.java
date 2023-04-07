package rest.eon.services;

import rest.eon.dto.TaskDto;
import rest.eon.models.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskService{
    List<Task> getAll();
    Task save(Task task);

    Optional<Task> getTaskById(String id);

    void delete(String id);

    Task mapToTask(TaskDto taskDto);

    TaskDto mapToTaskDto(Task task);

    Task update(Task task);


    List<Task> getTasks(String group_id, String start, String finish);
}
