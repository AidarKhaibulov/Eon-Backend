package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.repositories.TaskRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.TaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    @Override
    public Task save(Task task) {
        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<String> currentUserTasks = new ArrayList<>();
        if (currentUser.getTasks() != null) {
            currentUser.getTasks().stream().forEach(t->currentUserTasks.add(getTaskById(t).get().getId()));

            // checking if time of new task isn't already taken
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            LocalDateTime taskTimeStart=LocalDateTime.parse(task.getDateStart(),formatter);
            LocalDateTime taskTimeFinish=LocalDateTime.parse(task.getDateFinish(),formatter);
            for (var t : currentUserTasks) {
                Task cur=taskRepository.findById(t).get();
                LocalDateTime tTimeStart=LocalDateTime.parse(cur.getDateStart(),formatter);
                LocalDateTime tTimeFinish=LocalDateTime.parse(cur.getDateFinish(),formatter);
                // check if new task time doesn't interrupt existing task's times
                if (!cur.isCompleted() &&
                        ( (tTimeStart.isBefore(taskTimeStart) && tTimeFinish.isAfter(taskTimeStart)) ||
                         (tTimeStart.isBefore(taskTimeFinish) && tTimeFinish.isAfter(taskTimeFinish))  ||
                        tTimeStart.isEqual(taskTimeFinish) || tTimeStart.isEqual(taskTimeStart) ||
                                tTimeFinish.isEqual(taskTimeFinish) || tTimeFinish.isEqual(taskTimeStart))
                )
                    return null;
            }
        }
        Task savedTask = taskRepository.save(task);
        currentUserTasks.add(savedTask.getId());
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
        List<String> currentTasks = currentUser.getTasks();
        if(currentTasks.contains(id)) {
            currentTasks.remove(id);
            currentUser.setTasks(currentTasks);
            userRepository.save(currentUser);
            taskRepository.deleteById(id);
        }
        else throw new NoSuchElementException();
    }

    @Override
    public Task mapToTask(TaskDto taskDto) {
        return Task.builder()
                .id(taskDto.getId())
                .title(taskDto.getTitle())
                .dateStart(taskDto.getDateStart())
                .dateFinish(taskDto.getDateFinish())
                .userId(taskDto.getUserId())
                .groupId(taskDto.getGroupId())
                .isCompleted(taskDto.isCompleted())
                .build();
    }

    @Override
    public TaskDto mapToTaskDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .dateStart(task.getDateStart())
                .dateFinish(task.getDateFinish())
                .userId(task.getUserId())
                .groupId(task.getGroupId())
                .isCompleted(task.isCompleted())
                .build();
    }

    @Override
    public Task update(Task task) {
        return taskRepository.save(task);
    }
}
