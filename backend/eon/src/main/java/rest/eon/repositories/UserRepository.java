package rest.eon.repositories;

import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import rest.eon.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    List<User> findByNicknameContaining(String nickname);
    Optional<User>  getFirstByEmail(String email);
    Optional<User>  getFirstById(String email);
}
