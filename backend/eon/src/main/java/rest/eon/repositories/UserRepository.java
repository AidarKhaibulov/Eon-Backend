package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import rest.eon.models.User;

public interface UserRepository extends MongoRepository<User,String> {
    User findByLogin(String login);
}
