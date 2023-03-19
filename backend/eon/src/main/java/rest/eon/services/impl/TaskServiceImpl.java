package rest.eon.services.impl;

import org.springframework.stereotype.Service;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;
import rest.eon.repositories.TaskRepository;
import rest.eon.services.TaskService;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAll(){
        return taskRepository.findAll();
    }

    @Override
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }
    @Override
    public void delete(String id) {
        taskRepository.deleteById(id);
    }
    @Override
    public Task mapToTask(TaskDto taskDto){
        return Task.builder()
                .id(taskDto.getId())
                .title(taskDto.getTitle())
                .date(taskDto.getDate())
                .userId(taskDto.getUserId())
                .build();
    }
    @Override
    public TaskDto mapToTaskDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .date(task.getDate())
                .userId(task.getUserId())
                .build();
    }
}
