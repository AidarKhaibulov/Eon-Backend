package rest.eon.services.impl;

import com.mongodb.client.model.InsertOneOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.dto.NotificationDto;
import rest.eon.dto.TaskDto;
import rest.eon.models.Notification;
import rest.eon.models.Task;
import rest.eon.repositories.GroupRepository;
import rest.eon.repositories.NotificationRepository;
import rest.eon.repositories.TaskRepository;
import rest.eon.repositories.UserRepository;
import rest.eon.services.NotificationService;
import rest.eon.services.TaskService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final TaskService taskService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public Notification save(Notification notification) {
        Task linkedTask=taskService.getTaskById(notification.getTaskId()).get();
        Notification toSave=notificationRepository.save(notification);
        linkedTask.setNotificationId(toSave.getId());
        taskService.update(linkedTask);
        return toSave;
    }

    @Override
    public Optional<Notification> getById(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

}
