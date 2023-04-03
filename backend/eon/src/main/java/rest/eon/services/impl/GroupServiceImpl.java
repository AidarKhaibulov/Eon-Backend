package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.GroupDto;
import rest.eon.models.Group;
import rest.eon.models.Task;
import rest.eon.models.User;
import rest.eon.repositories.GroupRepository;
import rest.eon.repositories.TaskRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.GroupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    @Override
    public Group save(Group group) {
        Group newGroup = groupRepository.save(group);

        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        currentUser.setAdminGroups(List.of(newGroup.getId()));
        userRepository.save(currentUser);
        return newGroup;
    }

    @Override
    public Group mapToGroup(GroupDto dto) {
        return Group.builder()
                .name(dto.getName())
                .members(dto.getMembers())
                .admins(dto.getAdmins())
                .build();
    }

    @Override
    public Optional<Group> getGroupById(String id) {
        return groupRepository.findById(id);
    }
}
