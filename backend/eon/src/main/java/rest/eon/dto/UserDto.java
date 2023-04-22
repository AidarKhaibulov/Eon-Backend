package rest.eon.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDto {
    @Size(min=2,max=24,message="invalid nickname length")
    private String nickname;
    @Size(min=8,max=24,message="invalid password length")
    private String password;
    @Size(min=2,max=24,message="invalid firstname length")
    private String firstname;
    @Size(min=2,max=24,message="invalid lastname length")
    private String lastname;
    private List<String> photosUrl;

}