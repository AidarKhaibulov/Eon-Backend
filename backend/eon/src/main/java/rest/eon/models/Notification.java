package rest.eon.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("notification")
public class Notification {
    @Id
    private String id;
    @Indexed(unique = true)
    private String taskId;
    private String alarmBefore;
}
