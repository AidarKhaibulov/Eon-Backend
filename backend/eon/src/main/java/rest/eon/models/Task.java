package rest.eon.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("tasks")
public class Task {
    @Id
    private String id;
    private String date;
    private String title;

}