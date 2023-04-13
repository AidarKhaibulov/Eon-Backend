package rest.eon.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import rest.eon.models.Repetition;

public interface RepetitionRepository  extends MongoRepository<Repetition,String > {
}
