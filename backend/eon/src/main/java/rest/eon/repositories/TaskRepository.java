package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import rest.eon.models.Task;

public interface TaskRepository extends MongoRepository<Task,String>{


}
