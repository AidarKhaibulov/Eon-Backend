package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import rest.eon.models.Task;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task,String>{


    List<Task> findAllByNotificationIdIsNotNullAndDateStartIsGreaterThan(String time);



}
