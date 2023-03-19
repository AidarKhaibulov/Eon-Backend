package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import rest.eon.models.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
    Optional<User>  getFirstByEmail(String email);
}
