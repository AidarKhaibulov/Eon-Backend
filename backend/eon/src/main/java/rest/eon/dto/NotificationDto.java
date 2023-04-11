package rest.eon.dto;

import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
@Data
@Builder
public class NotificationDto {
    private Integer alarmBefore;
    private String unitsType;

}
