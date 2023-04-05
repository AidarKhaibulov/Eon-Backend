package rest.eon.controllers;

import com.mongodb.client.model.Collation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.services.GroupService;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;

import java.util.*;

import static com.mongodb.client.model.Sorts.ascending;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;
    final private GroupService groupService;
    @Data
    @AllArgsConstructor
    static
    class TaskOptions{
        private String sortingMethod;
        private String dateStart;
        private String dateFinish;
    }
    @GetMapping()
    List<Task> fetchTasks(@RequestBody TaskOptions options) {
        List<Task> l= getTasks(null);
        Collections.sort(l, Comparator.comparing(Task::getDate));
        //Collections.sort(l, (a,b)->b.getDate().compareTo(a.getDate()));
        return l;
    }

    @GetMapping("/{group_id}")
    List<Task> fetchTasksFromGroup(@RequestBody TaskOptions options,@PathVariable String group_id) {
        List<Task> l= getTasks(group_id);
        Collections.sort(l, Comparator.comparing(Task::getDate));
        return l;
    }

    @PostMapping()
    ResponseEntity<Task> createTask(@Valid @RequestBody TaskDto task) {
        return addNewTask(task,null);
    }

    @PostMapping("/{group_id}")
    ResponseEntity<?> createTaskInGroup(@Valid @RequestBody TaskDto task, @PathVariable String group_id) {
        if(userNotGroupAdmin(groupService.getGroupById(group_id).get()))
            return new ResponseEntity<>("Such a group not found",HttpStatus.FORBIDDEN);
        return addNewTask(task,group_id);
    }

    @PutMapping("/{id}")
    ResponseEntity<Task> editTask(@Valid @RequestBody TaskDto newTask, @PathVariable String id) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        return taskService.getTaskById(id).map(task -> {
            task.setDate(newTask.getDate());
            task.setTitle(newTask.getTitle());
            task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
            logger.info("Task with id " + id + " has been updated!");
            return ResponseEntity.ok(taskService.save(task));
        }).orElseGet(() -> ResponseEntity.ok(taskService.save(taskService.mapToTask(newTask))));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteTask(@PathVariable String id) {
        try {
            Task taskToDelete = taskService.getTaskById(id).orElse(null);
            if (taskToDelete == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else {
                taskService.delete(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    private ResponseEntity<Task> addNewTask(TaskDto task,String group_id) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        task.setUserId(userService.getUserIdByEmail(currentUserEmail).get().getId());
        task.setGroupId(group_id);
        task.setCompleted(false);
        Task createdTask = taskService.save(taskService.mapToTask(task));
        if (createdTask != null) return ResponseEntity.ok(createdTask);
        else return null;
    }

    private boolean userNotGroupAdmin(Group currentGroup) {
        return !currentGroup.getAdmins().contains(userService.getUserByEmail(SecurityUtil.getSessionUser()).get().getId());
    }

    private List<Task> getTasks(String group_id) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        User user = userService.getUserByEmail(currentUserEmail).get();
        List<Task> tasks = new ArrayList<>();
        if(group_id!=null){
            user.getTasks().forEach(t ->{
                Task cur=taskService.getTaskById(t).get();
                if(cur.getGroupId()!=null && cur.getGroupId().equals(group_id))
                    tasks.add(taskService.getTaskById(t).get());
            } );
        }
        else
            user.getTasks().forEach(t -> tasks.add(taskService.getTaskById(t).get()));
        return tasks;
    }
}
