package rest.eon.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.GroupDto;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.services.GroupService;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController()
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;
    final private GroupService groupService;
    @PostMapping()
    ResponseEntity<Group> createGroup(@Valid @RequestBody GroupDto group) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        group.setAdmins(List.of(userService.getUserByEmail(currentUserEmail).get().getId()));
        Group newGroup = groupService.save(groupService.mapToGroup(group));
        if (newGroup != null)
            return ResponseEntity.ok(newGroup);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> editGroup(@Valid @RequestBody GroupDto group, @PathVariable String id) {
        Group currentGroup=groupService.getGroupById(id).orElse(null);
        if (currentGroup == null)
            return new ResponseEntity<>("Such a group not found",HttpStatus.FORBIDDEN);
        else {
            User u=userService.getUserByEmail(SecurityUtil.getSessionUser()).get();
            List<String> userGroups=u.getAdminGroups();
            if(userGroups.stream().noneMatch(x->x.equals(id)))
                return new ResponseEntity<>("Such a group not found",HttpStatus.FORBIDDEN);
            else
                return groupService.getGroupById(id).map(g -> {
                    g.setName(group.getName());
                    logger.info("Group with id " + id + " has been updated!");
                    return ResponseEntity.ok(groupService.save(g));
                }).get();
        }
    }
}
