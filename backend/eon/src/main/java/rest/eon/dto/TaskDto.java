package rest.eon.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@Builder
public class TaskDto {
    private String id;
    @NotNull(message = "Date should not be null")
    private String date;

    @NotEmpty(message = "Title should not be empty")
    private String title;
    private String userId;
    private String groupId;
    private boolean isCompleted;

}