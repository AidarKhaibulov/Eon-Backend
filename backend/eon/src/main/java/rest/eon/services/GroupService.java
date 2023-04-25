package rest.eon.services;

import rest.eon.dto.GroupDto;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Task;
import rest.eon.models.User;

import java.util.List;
import java.util.Optional;

public interface GroupService {
    Group save(Group group);
    Group mapToGroup(GroupDto dto);

    Optional<Group> getGroupById(String id);

    void delete(String id);

    List<String > getAllMembershipGroups(User user);

    Group getGroupInfoById(String groupId, User user);
}
