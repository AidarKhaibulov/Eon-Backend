package rest.eon.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("tasks")
public class Task {
    @Id
    private String id;
    private String dateStart;
    private String dateFinish;
    private String title;
    private String userId;
    private String groupId;
    private boolean isCompleted;

    /**
     * Represents user id which current task assigned to
     */
    private String assignedTo;
    private String notificationId;
    private String repetitionId;

}