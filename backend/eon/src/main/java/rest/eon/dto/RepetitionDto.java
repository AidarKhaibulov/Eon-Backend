package rest.eon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepetitionDto {

    @NotNull
    private String repetitionDays;
    @NotNull
    private String repetitionCount;
}
