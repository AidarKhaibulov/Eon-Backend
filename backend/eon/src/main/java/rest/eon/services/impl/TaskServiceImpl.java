package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Repetition;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.repositories.GroupRepository;
import rest.eon.repositories.RepetitionRepository;
import rest.eon.repositories.TaskRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.TaskService;

import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RepetitionRepository repetitionRepository;
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public List<Task> getRelevantTasksWithEnabledNotifications() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date());
        return taskRepository.findAllByNotificationIdIsNotNullAndDateStartIsGreaterThan(timeStamp);
    }

    @Override
    public Task save(Task task) throws InvalidObjectException {
        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<String> currentUserTasks = new ArrayList<>();
        if (currentUser.getTasks() != null) {
            currentUser.getTasks().forEach(t -> currentUserTasks.add(getTaskById(t).get().getId()));

            if (isNewTaskTimeOverlapsExistingOnes(task, currentUserTasks)) throw new InvalidObjectException("Time of this task interrupts existing one");

        }
        Task savedTask = taskRepository.save(task);
        currentUserTasks.add(savedTask.getId());
        currentUser.setTasks(currentUserTasks);
        userRepository.save(currentUser);
        return savedTask;
    }

    /**
     * Checks if time of {@currentTask} isn't already taken by existing tasks from {@tasks}
     */
    private boolean isNewTaskTimeOverlapsExistingOnes(Task newTask, List<String> tasks) {

        for (var t : tasks) {
            Task old = taskRepository.findById(t).get();
            if (twoTasksIntersect(newTask, old))
                return true;
        }
        return false;

    }

    public boolean twoTasksIntersect(Task newT, Task oldT) {

        LocalDateTime taskTimeStart = LocalDateTime.parse(newT.getDateStart(), format);
        LocalDateTime taskTimeFinish = LocalDateTime.parse(newT.getDateFinish(), format);

        LocalDateTime tTimeStart = LocalDateTime.parse(oldT.getDateStart(), format);
        LocalDateTime tTimeFinish = LocalDateTime.parse(oldT.getDateFinish(), format);

        // check if new task time doesn't interrupt existing task's times
        if (!oldT.isCompleted() &&
                ((tTimeStart.isBefore(taskTimeStart) && tTimeFinish.isAfter(taskTimeStart)) ||
                        (tTimeStart.isBefore(taskTimeFinish) && tTimeFinish.isAfter(taskTimeFinish)) ||
                        tTimeStart.isEqual(taskTimeFinish) || tTimeStart.isEqual(taskTimeStart) ||
                        tTimeFinish.isEqual(taskTimeFinish) || tTimeFinish.isEqual(taskTimeStart)))
            return true;

        boolean isNewRepeatable = newT.getRepetitionId() != null;
        boolean isOldRepeatable = oldT.getRepetitionId() != null;
        // One of two tasks is repeatable or they both are
        if (isNewRepeatable ||  isOldRepeatable) {
            // extract only time
            LocalTime newTimeStart = LocalTime.parse(newT.getDateStart().substring(11, 16));
            LocalTime newTimeFinish = LocalTime.parse(newT.getDateFinish().substring(11, 16));
            LocalTime oldTimeStart = LocalTime.parse(oldT.getDateStart().substring(11, 16));
            LocalTime oldTimeFinish = LocalTime.parse(oldT.getDateFinish().substring(11, 16));

            // check only if times of these two tasks intersect
            if ((newTimeStart.isBefore(oldTimeStart) && newTimeFinish.isAfter(oldTimeStart)) ||
                            (newTimeStart.isBefore(oldTimeFinish) && newTimeFinish.isAfter(oldTimeFinish)) ||
                    newTimeStart.equals(oldTimeFinish) || newTimeStart.equals(oldTimeStart) ||
                    newTimeFinish.equals(oldTimeFinish) || newTimeFinish.equals(oldTimeStart))
            {
                // check if days of week intersect
                List<String> forbiddenDaysForOld=null;
                List<String> forbiddenDaysForNew=null;
                // extracting already occupied days for both tasks
                if(isNewRepeatable){
                    Repetition rep=repetitionRepository.findById(newT.getRepetitionId()).get();
                    forbiddenDaysForOld=rep.getRepetitionSchema();
                }
                else{
                    Repetition rep=repetitionRepository.findById(oldT.getRepetitionId()).get();
                    forbiddenDaysForNew=rep.getRepetitionSchema();
                }


                if(isNewRepeatable && !isOldRepeatable){
                    DayOfWeek oldTDay=LocalDate.parse(oldT.getDateStart().substring(0,10)).getDayOfWeek();
                    if(forbiddenDaysForOld.contains(oldTDay.toString()))
                        return true;
                }
                else
                if(!isNewRepeatable && isOldRepeatable){
                    DayOfWeek newTDay= LocalDate.parse(newT.getDateStart().substring(0,10)).getDayOfWeek();
                    if(forbiddenDaysForNew.contains(newTDay.toString()))
                        return true;
                }
                // both tasks are repeatable
                else{
                    for(String dayI: forbiddenDaysForNew)
                        for(String dayJ: forbiddenDaysForOld)
                            if(dayJ.equals(dayI))
                                return true;
                }
            }

        }

        return false;

    }

    @Override
    public Task update(Task task) {
        if (task.getDateStart().compareTo(task.getDateFinish()) > 0) return null;
        // check edited task time doesn't interrupt existing tasks time
        User u = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<String> userTasks = u.getTasks();
        if (userTasks != null) {
            // we need to remove current task from tasks list, because new task time interval can intersect old task time interval
            userTasks.remove(task.getId());

            if (isNewTaskTimeOverlapsExistingOnes(task, userTasks)) return null;
        }
        return taskRepository.save(task);

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
                .description(taskDto.getDescription())
                .photosUrl(taskDto.getPhotosUrl())
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
    public List<Task> getTasks(String group_id, String start, String finish) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        User user = userRepository.getFirstByEmail(currentUserEmail).get();
        List<Task> tasks;

        // case when need to return only task from specified group
        if (group_id != null) {
            tasks = new ArrayList<>();

            // fetching those tasks which user created in group as administrator
            if (user.getTasks() != null) user.getTasks().forEach(t -> {

                //check time restriction
                Task cur = taskRepository.findById(t).get();


                if (cur.getGroupId() != null && cur.getGroupId().equals(group_id) && isSatisfiedTimeRestriction(cur, start, finish))
                    tasks.add(taskRepository.findById(t).get());
            });

                // fetching those tasks which user has in group as member
            else if (user.getMembershipGroups() != null && user.getMembershipGroups().contains(group_id)) {
                Group g = groupRepository.findById(group_id).get();
                g.getTasks().forEach(t -> {
                    Task cur = taskRepository.findById(t).get();
                    if (isSatisfiedTimeRestriction(cur, start, finish)) tasks.add(cur);
                });
            }

            //else will be returned []

        }

        // case when need to return all the tasks
        else {
            tasks = new ArrayList<>();
            if (user.getTasks() != null) {
                user.getTasks().forEach(t -> {
                    Task cur = taskRepository.findById(t).get();
                    if (isSatisfiedTimeRestriction(cur, start, finish)) tasks.add(cur);
                });

            }
        }
        return tasks;

    }

    @Override
    public void sortTasks(List<Task> l, String method) {
        if (method == null || method.equals("")) method = "default";
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

    /**
     * Checks if current task {@cur} lies between {@start} time and {@finish} time
     */
    private boolean isSatisfiedTimeRestriction(Task cur, String start, String finish) {
        if (start == null || finish == null) return true;
        LocalDateTime cStart = LocalDateTime.parse(cur.getDateStart(), format);
        LocalDateTime cFinish = LocalDateTime.parse(cur.getDateFinish(), format);
        LocalDateTime s = LocalDateTime.parse(start, format);
        LocalDateTime f = LocalDateTime.parse(finish, format);
        return ((cStart.isAfter(s) || cStart.isEqual(s)) && (cFinish.isBefore(f) || cFinish.isEqual(f)));
    }

    @Override
    public ResponseEntity<Task> addNewTask(TaskDto task, String group_id) throws InvalidObjectException {
        // checking if start time < finish time, or they are equal
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime start = LocalDateTime.parse(task.getDateStart(), formatter);
        LocalDateTime finish = LocalDateTime.parse(task.getDateFinish(), formatter);
        if (start.isAfter(finish) || start.equals(finish)) throw new InvalidObjectException("Time of this task interrupts existing one");
        String currentUserEmail = SecurityUtil.getSessionUser();
        task.setUserId(userRepository.findByEmail(currentUserEmail).get().getId());
        task.setGroupId(group_id);
        task.setCompleted(false);
        task.setNotificationId(task.getNotificationId());
        task.setPhotosUrl(task.getPhotosUrl());
        Task createdTask = save(mapToTask(task));

        // updating group's task field
        if (group_id != null) {
            Group g = groupRepository.findById(group_id).get();
            HashSet<String> tasks;
            if (g.getTasks() == null) tasks = new HashSet<>(Collections.singleton(createdTask.getId()));
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
