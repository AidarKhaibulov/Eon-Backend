package rest.eon.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("tokens")
@NoArgsConstructor
@AllArgsConstructor

public class Token {

    @Id
    public Integer id;

    @Indexed(unique = true)
    public String token;

    public String tokenType= "BEARER";
//    public TokenType tokenType = TokenType.BEARER;

    public boolean revoked;

    public boolean expired;

    public String userId;
}