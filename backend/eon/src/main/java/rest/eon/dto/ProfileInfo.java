package rest.eon.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileInfo {
    private String nickname;
    private String firstname;
    private String lastname;
    private String id;
}
