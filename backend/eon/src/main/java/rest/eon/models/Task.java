package rest.eon.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@Document("tasks")
public class Task {
    @Id
    private String id;
    private LocalDateTime date;
    private String title;
    private String userId;

}