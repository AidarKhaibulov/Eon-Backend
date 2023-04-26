package rest.eon.services.impl;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;
import rest.eon.dto.ProfileInfo;
import rest.eon.models.User;
import rest.eon.repositories.UserRepository;
import rest.eon.services.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserIdByEmail(String email) {
        return userRepository.getFirstByEmail(email);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteByEmail(String email) {
        userRepository.delete(getUserByEmail(email).get());
    }

    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.getFirstById(userId);
    }

    @Override
    public List<ProfileInfo> findByNickname(String nickname) {
        List<User> users=userRepository.findByNicknameContaining(nickname);
        if (users != null)
            return users.stream().map(this::mapToProfileInfo).toList();
        else throw new RuntimeException("No matches");
    }

    @Override
    public ProfileInfo findById(String id) {
        return mapToProfileInfo(userRepository.findById(id).orElseThrow());
    }
    private ProfileInfo mapToProfileInfo(User user){
        return ProfileInfo.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .nickname(user.getNickname())
                .build();
    }
}
