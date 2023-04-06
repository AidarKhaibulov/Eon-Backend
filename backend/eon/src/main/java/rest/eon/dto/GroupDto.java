package rest.eon.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import rest.eon.models.User;

import java.util.List;

@Data
@Builder
public class GroupDto {

    @NotEmpty(message = "Name should not be empty")
    private String name;
    private List<String > members;
    private List<String > admins;
    private List<String > tasks;

}
