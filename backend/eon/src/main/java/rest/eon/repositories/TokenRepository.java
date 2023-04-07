package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import rest.eon.models.Token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {
    @Query("{'user_id': ?0}")
    List<Token> findAllByUserIdAndExpiredIsFalseAndRevokedIsFalse(String user_id);
    Optional<Token> findByToken(String token);
}