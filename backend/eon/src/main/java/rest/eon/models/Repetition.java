package rest.eon.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document("repetitions")
public class Repetition {
    private String id;
    @Indexed(unique = true)
    private String taskId;
    private List<String> repetitionSchema;

}
