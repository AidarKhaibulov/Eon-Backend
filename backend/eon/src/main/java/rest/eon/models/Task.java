package rest.eon.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Builder;

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

}