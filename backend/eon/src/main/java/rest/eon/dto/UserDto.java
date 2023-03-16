package rest.eon.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String id;
    @NotEmpty(message = "login should not be empty")
    private String login;
    @NotEmpty(message = "password should not be empty")
    private String password;
    @NotEmpty(message = "username should not be empty")
    private String username;


}