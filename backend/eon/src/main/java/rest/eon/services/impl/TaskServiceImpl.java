package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.repositories.GroupRepository;
import rest.eon.repositories.TaskRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.TaskService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public List<Task> getRelevantTasksWithEnabledNotifications(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date());
        return taskRepository.findAllByNotificationIdIsNotNullAndDateStartIsGreaterThan(timeStamp);
    }

    @Override
    public Task save(Task task) {
        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<String> currentUserTasks = new ArrayList<>();
        if (currentUser.getTasks() != null) {
            currentUser.getTasks().stream().forEach(t -> currentUserTasks.add(getTaskById(t).get().getId()));

            // checking if time of new task isn't already taken
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

            LocalDateTime taskTimeStart = LocalDateTime.parse(task.getDateStart(), formatter);
            LocalDateTime taskTimeFinish = LocalDateTime.parse(task.getDateFinish(), formatter);
            for (var t : currentUserTasks) {
                Task cur = taskRepository.findById(t).get();
                LocalDateTime tTimeStart = LocalDateTime.parse(cur.getDateStart(), formatter);
                LocalDateTime tTimeFinish = LocalDateTime.parse(cur.getDateFinish(), formatter);

                // check if new task time doesn't interrupt existing task's times
                if (!cur.isCompleted() &&
                        ((tTimeStart.isBefore(taskTimeStart) && tTimeFinish.isAfter(taskTimeStart)) ||
                                (tTimeStart.isBefore(taskTimeFinish) && tTimeFinish.isAfter(taskTimeFinish)) ||
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
        if (currentTasks.contains(id)) {
            currentTasks.remove(id);
            currentUser.setTasks(currentTasks);
            userRepository.save(currentUser);
            taskRepository.deleteById(id);
        } else throw new NoSuchElementException();
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
                .assignedTo(taskDto.getAssignedTo())
                .notificationId(taskDto.getNotificationId())
                .build();
    }

    @Override
    public Task update(Task task) {
        // check edited task time doesn't interrupt existing tasks time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        User u = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        LocalDateTime taskTimeStart = LocalDateTime.parse(task.getDateStart(), formatter);
        LocalDateTime taskTimeFinish = LocalDateTime.parse(task.getDateFinish(), formatter);
        if (u.getTasks() != null)
            for (var t : u.getTasks()) {
                Task cur = taskRepository.findById(t).get();

                // case when we encounter currently edited task
                if(cur.getId().equals(task.getId()))
                    continue;

                LocalDateTime tTimeStart = LocalDateTime.parse(cur.getDateStart(), formatter);
                LocalDateTime tTimeFinish = LocalDateTime.parse(cur.getDateFinish(), formatter);

                // check if new task time doesn't interrupt existing task's times
                if (!cur.isCompleted() &&
                        ((tTimeStart.isBefore(taskTimeStart) && tTimeFinish.isAfter(taskTimeStart)) ||
                                (tTimeStart.isBefore(taskTimeFinish) && tTimeFinish.isAfter(taskTimeFinish)) ||
                                tTimeStart.isEqual(taskTimeFinish) || tTimeStart.isEqual(taskTimeStart) ||
                                tTimeFinish.isEqual(taskTimeFinish) || tTimeFinish.isEqual(taskTimeStart))
                )
                    return null;
            }

        return taskRepository.save(task);
    }

    @Override
    public List<Task> getTasks(String group_id, String start, String finish) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        User user = userRepository.getFirstByEmail(currentUserEmail).get();
        List<Task> tasks;

        // case when need to return only task from specified group
        if (group_id != null) {
            tasks = new ArrayList<>();

            // fetching those tasks which user created in group as administrator
            if (user.getTasks() != null)
                user.getTasks().forEach(t -> {

                    //check time restriction
                    Task cur = taskRepository.findById(t).get();


                    if (cur.getGroupId() != null &&
                            cur.getGroupId().equals(group_id) &&
                            isSatisfiedTimeRestriction(cur, start, finish))
                        tasks.add(taskRepository.findById(t).get());
                });

                // fetching those tasks which user has in group as member
            else if (user.getMembershipGroups() != null && user.getMembershipGroups().contains(group_id)) {
                Group g = groupRepository.findById(group_id).get();
                g.getTasks().forEach(t -> {
                    Task cur = taskRepository.findById(t).get();
                    if (isSatisfiedTimeRestriction(cur, start, finish))
                        tasks.add(cur);
                });
            }

            //else return []

        }

        // case when need to return all the tasks
        else {
            tasks = new ArrayList<>();
            if (user.getTasks() != null) {
                user.getTasks().forEach(t -> {
                    Task cur = taskRepository.findById(t).get();
                    if (isSatisfiedTimeRestriction(cur, start, finish))
                        tasks.add(cur);
                });

            }
        }
        return tasks;

    }

    @Override
    public void sortTasks(List<Task> l, String method) {
        DateTimeFormatter f;
        if (method == null || method.equals(""))
            method = "default";
        switch (method) {
            case "default" -> l.sort(Comparator.comparing(Task::getDateStart));
            case "defaultDesc" -> l.sort((a, b) -> b.getDateStart().compareTo(a.getDateStart()));
            case "name" -> l.sort(Comparator.comparing(Task::getTitle));
            case "nameDesc" -> l.sort((a, b) -> b.getTitle().compareTo(a.getTitle()));
            case "day" -> l.sort(Comparator.comparing(a -> a.getDateStart().substring(8, 10)));
            case "dayDesc" ->
                    l.sort((a, b) -> b.getDateStart().substring(8, 10).compareTo(a.getDateStart().substring(8, 10)));
            case "month" -> l.sort(Comparator.comparing(a -> a.getDateStart().substring(5, 7)));
            case "monthDesc" ->
                    l.sort((a, b) -> b.getDateStart().substring(5, 7).compareTo(a.getDateStart().substring(5, 7)));
            case "year" -> l.sort(Comparator.comparing(a -> a.getDateStart().substring(0, 4)));
            case "yearDesc" ->
                    l.sort((a, b) -> b.getDateStart().substring(0, 4).compareTo(a.getDateStart().substring(0, 4)));
        }
    }


    private boolean isSatisfiedTimeRestriction(Task cur, String start, String finish) {
        if (start == null || finish == null)
            return true;
        LocalDateTime cStart = LocalDateTime.parse(cur.getDateStart(), format);
        LocalDateTime cFinish = LocalDateTime.parse(cur.getDateFinish(), format);
        LocalDateTime s = LocalDateTime.parse(start, format);
        LocalDateTime f = LocalDateTime.parse(finish, format);
        return ((cStart.isAfter(s) || cStart.isEqual(s)) &&
                (cFinish.isBefore(f) || cFinish.isEqual(f)));
    }

    @Override
    public ResponseEntity<Task> addNewTask(TaskDto task, String group_id) {
        // checking if start time < finish time, or they are equal
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime start = LocalDateTime.parse(task.getDateStart(), formatter);
        LocalDateTime finish = LocalDateTime.parse(task.getDateFinish(), formatter);

        if (start.isAfter(finish) || start.equals(finish)) return null;
        String currentUserEmail = SecurityUtil.getSessionUser();
        task.setUserId(userRepository.findByEmail(currentUserEmail).get().getId());
        task.setGroupId(group_id);
        task.setCompleted(false);
        task.setNotificationId(task.getNotificationId());
        Task createdTask = save(mapToTask(task));

        // updating group's task field
        if (group_id != null) {
            Group g = groupRepository.findById(group_id).get();
            HashSet<String> tasks;
            if (g.getTasks() == null)
                tasks = new HashSet<>(Collections.singleton(createdTask.getId()));
            else {
                tasks = new HashSet<>(g.getTasks());
                tasks.add(createdTask.getId());
            }
            g.setTasks(new ArrayList<>(tasks));
            groupRepository.save(g);
        }

        return ResponseEntity.ok(createdTask);
    }


}
