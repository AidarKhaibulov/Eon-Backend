package rest.eon.services;

import rest.eon.models.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService{
    List<Task> getAll();
    Task save(Task task);

    Optional<Task> getTaskById(String id);

    void delete(String id);
}
