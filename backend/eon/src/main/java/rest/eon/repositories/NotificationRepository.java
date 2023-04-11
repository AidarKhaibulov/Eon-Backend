package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import rest.eon.models.Notification;

public interface NotificationRepository extends MongoRepository<Notification,String> {
}
