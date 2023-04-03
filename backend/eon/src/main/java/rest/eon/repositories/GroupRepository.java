package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import rest.eon.models.Group;

public interface GroupRepository extends MongoRepository<Group,String > {
}
