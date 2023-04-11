package rest.eon.services;

import rest.eon.dto.NotificationDto;
import rest.eon.dto.TaskDto;
import rest.eon.models.Notification;
import rest.eon.models.Task;

import java.util.Optional;

public interface NotificationService {
    Notification save(Notification notification);

    Optional<Notification >getById(String notificationId);
}
