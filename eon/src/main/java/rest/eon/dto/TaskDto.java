package rest.eon.dto;


import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskDto {
    private String id;
    @NotEmpty(message = "Date should not be empty")
    private LocalDateTime date;
    @NotEmpty(message = "Title should not be empty")
    private String title;

}