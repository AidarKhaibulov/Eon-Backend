package rest.eon.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Size(min=2,max=24,message="invalid nickname length")
    private String nickname;
    @Size(min=2,max=24,message="invalid firstname length")
    private String firstname;
    @Size(min=2,max=24,message="invalid lastname length")
    private String lastname;
    @Email
    private String email;
    @Size(min=8,max=24,message="invalid password length")
    private String password;


}