package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.auth.SecurityUtil;
import rest.eon.dto.GroupDto;
import rest.eon.models.Group;
import rest.eon.models.User;
import rest.eon.repositories.GroupRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.GroupService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Override
    public Group save(Group group) {
        Group newGroup = groupRepository.save(group);

        User currentUser = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        HashSet<String> admins=null;
        if(currentUser.getAdminGroups()!=null) {
            admins = new HashSet<>(currentUser.getAdminGroups());
            admins.add(newGroup.getId());
        }
        else admins=new HashSet<>(Collections.singleton(newGroup.getId()));
        currentUser.setAdminGroups(new ArrayList<>(admins));
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

    @Override
    public void delete(String id) {
        User u = userRepository.findByEmail(SecurityUtil.getSessionUser()).get();
        List<String> groups = u.getAdminGroups();
        groups.remove(id);
        u.setAdminGroups(groups);
        userRepository.save(u);
        groupRepository.deleteById(id);
    }
}
