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
import rest.eon.models.Group;
import rest.eon.models.User;
import rest.eon.services.GroupService;
import rest.eon.services.TaskService;
import rest.eon.services.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    final static Logger logger = LoggerFactory.getLogger(TaskController.class);
    final private TaskService taskService;
    final private UserService userService;
    final private GroupService groupService;

    private static ResponseEntity<String> NotFoundEntity() {
        return new ResponseEntity<>("Such a group not found", HttpStatus.FORBIDDEN);
    }

    private boolean userNotGroupAdmin(Group currentGroup) {
        return !currentGroup.getAdmins().contains(userService.getUserByEmail(SecurityUtil.getSessionUser()).get().getId());
    }

    @GetMapping()
    List<Group> fetchAdministratedGroups() {
        String currentUserEmail = SecurityUtil.getSessionUser();
        User user = userService.getUserByEmail(currentUserEmail).get();
        List<Group> groups = new ArrayList<>();
        if (user.getAdminGroups() != null)
            user.getAdminGroups().forEach(g -> groups.add(groupService.getGroupById(g).get()));
        return groups;
    }

    @PostMapping()
    ResponseEntity<Group> createGroup(@Valid @RequestBody GroupDto group) {
        String currentUserEmail = SecurityUtil.getSessionUser();
        group.setAdmins(List.of(userService.getUserByEmail(currentUserEmail).get().getId()));
        Group newGroup = groupService.save(groupService.mapToGroup(group));
        if (newGroup != null) return ResponseEntity.ok(newGroup);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{group_id}/addMembers")
    ResponseEntity<?> addMembers(@Valid @RequestBody List<String> users, @PathVariable String group_id) {
        return addMemberToGroup(group_id, users);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> editGroup(@Valid @RequestBody GroupDto group, @PathVariable String id) {
        Group currentGroup = groupService.getGroupById(id).orElse(null);
        if (currentGroup == null) return NotFoundEntity();
        else {
            User u = userService.getUserByEmail(SecurityUtil.getSessionUser()).get();
            List<String> userGroups = u.getAdminGroups();
            if (userGroups == null || userGroups.stream().noneMatch(x -> x.equals(id))) return NotFoundEntity();
            else return groupService.getGroupById(id).map(g -> {
                g.setName(group.getName());
                logger.info("Group with id " + id + " has been updated!");
                return ResponseEntity.ok(groupService.save(g));
            }).get();
        }
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteGroup(@PathVariable String id) {
        Group g = groupService.getGroupById(id).orElse(null);
        if (g == null) return NotFoundEntity();
        else {
            User u = userService.getUserByEmail(SecurityUtil.getSessionUser()).get();
            List<String> userGroups = u.getAdminGroups();
            if (userGroups == null || userGroups.stream().noneMatch(x -> x.equals(id))) return NotFoundEntity();
            groupService.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }

    }

    @DeleteMapping("/{group_id}/deleteMembers")
    ResponseEntity<?> deleteMembers(@Valid @RequestBody List<String> users, @PathVariable String group_id) {
        return deleteMembersFromGroup(group_id, users);
    }

    private ResponseEntity<?> deleteMembersFromGroup(String groupId, List<String> users) {
        Group currentGroup = groupService.getGroupById(groupId).orElse(null);
        User u = userService.getUserByEmail(SecurityUtil.getSessionUser()).get();

        if (currentGroup == null) return NotFoundEntity();
        else {
            if (userNotGroupAdmin(currentGroup)) return NotFoundEntity();
            for (String userId : users) {

                User member = userService.getUserById(userId).orElse(null);
                if (member == null || member.getMembershipGroups() == null || !member.getMembershipGroups().contains(groupId))
                    return new ResponseEntity<>("Provided user not found in this group", HttpStatus.FORBIDDEN);

                if (userId.equals(u.getId()))
                    return new ResponseEntity<>("Cannot delete yourself from group", HttpStatus.FORBIDDEN);

                // handling user's groups list
                List<String> memberList = member.getMembershipGroups();
                memberList.remove(groupId);
                member.setMembershipGroups(memberList);
                userService.save(member);

                // handling group's members list
                List<String> members = currentGroup.getMembers();
                members.remove(userId);
                currentGroup.setMembers(members);
                groupService.save(currentGroup);
            }
            return ResponseEntity.ok(groupService.getGroupById(currentGroup.getId()));
        }
    }

    private ResponseEntity<?> addMemberToGroup(String group_id, List<String> user_ids) {
        Group currentGroup = groupService.getGroupById(group_id).orElse(null);
        User u = userService.getUserByEmail(SecurityUtil.getSessionUser()).get();

        if (currentGroup == null) return NotFoundEntity();
        else {
            if (userNotGroupAdmin(currentGroup)) return NotFoundEntity();
            for (String user_id : user_ids) {

                if (userService.getUserById(user_id).isEmpty())
                    return new ResponseEntity<>("Provided user not found", HttpStatus.FORBIDDEN);

                // handling group's members list
                List<String> members = currentGroup.getMembers();
                if (members == null) members = new ArrayList<>();
                if (!members.contains(user_id)) members.add(user_id);
                currentGroup.setMembers(members);
                groupService.save(currentGroup);

                // handling user's groups list
                User member = userService.getUserById(user_id).get();
                HashSet<String> memberList;
                if (member.getMembershipGroups() != null) {
                    memberList = new HashSet<>(member.getMembershipGroups());
                    memberList.add(group_id);
                } else memberList = new HashSet<>(Collections.singleton(group_id));
                member.setMembershipGroups(new ArrayList<>(memberList));
                userService.save(member);

            }
            return ResponseEntity.ok(groupService.getGroupById(currentGroup.getId()));
        }
    }
}
