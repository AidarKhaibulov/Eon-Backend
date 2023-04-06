package rest.eon.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document("groups")
public class Group {
    @Id
    private String id;
    private String name;
    private List<String> members;
    private List<String> admins;

    private List<String> tasks;
}
