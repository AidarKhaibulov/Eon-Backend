package rest.eon.services.impl;

import org.springframework.stereotype.Service;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.repositories.TaskRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    @Override
    public Task save(Task task) {

        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<Task> currentUserTasks = currentUser.getTasks();
        if (currentUserTasks == null)
            currentUserTasks = new ArrayList<>();
        for (var o : currentUserTasks) {
            if (o.getDate().equals(task.getDate()))
                return null;
        }
        Task savedTask = taskRepository.save(task);
        currentUserTasks.add(savedTask);
        currentUser.setTasks(currentUserTasks);
        userRepository.save(currentUser);
        return savedTask;
    }

    @Override
    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    @Override
    public void delete(String id) {
        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<Task> currentTasks = currentUser.getTasks();
        currentTasks.remove(currentTasks.stream().findFirst().filter(t->t.getId().equals(id)).get());
        currentUser.setTasks(currentTasks);
        userRepository.save(currentUser);
        taskRepository.deleteById(id);
    }

    @Override
    public Task mapToTask(TaskDto taskDto) {
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
