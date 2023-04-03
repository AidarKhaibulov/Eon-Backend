package rest.eon.services;

import rest.eon.dto.GroupDto;
import rest.eon.dto.TaskDto;
import rest.eon.models.Group;
import rest.eon.models.Task;

import java.util.Optional;

public interface GroupService {
    Group save(Group group);
    Group mapToGroup(GroupDto dto);

    Optional<Group> getGroupById(String id);
}
