package rest.eon.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskDto {
    @Null
    private String id;
    @NotNull(message = "Date should not be null")
    private String dateStart;
    @NotNull(message = "Date should not be null")
    private String dateFinish;
    @NotEmpty(message = "Title should not be empty")
    private String title;
    @Null
    private String userId;
    @Null
    private String groupId;
    private boolean isCompleted;

}