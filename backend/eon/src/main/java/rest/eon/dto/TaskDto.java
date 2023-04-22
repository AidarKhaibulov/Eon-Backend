package rest.eon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Task information")
public class TaskDto {
    @Null
    private String id;
    @NotNull(message = "Date should not be null")
    @Schema(description = "When task starts")
    private String dateStart;
    @NotNull(message = "Date should not be null")
    @Schema(description = "When task ends")
    private String dateFinish;
    @NotEmpty(message = "Title should not be empty")
    @Schema(description = "Task's title")
    private String title;
    @NotEmpty(message = "Description should not be empty")
    @Schema(description = "Task's description")
    private String description;
    private List<String> photosUrl;

    @Null
    private String userId;
    @Null
    private String groupId;
    private boolean isCompleted;
    private String assignedTo;
    @Null
    private String notificationId;


}