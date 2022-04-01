package ru.somarov.deletion_service.domain.entity.jsonb.types;

import com.fasterxml.jackson.core.type.TypeReference;
import ru.somarov.deletion_service.domain.entity.jsonb.Completion;

/**
 * This class is a database data type for JSONB structure
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 * @see Completion
 *
 */
public class CompletionPgJsonPropertyType extends PgJsonCollection<Completion> {

    @Override
    protected TypeReference<Completion> getReturnedTypeReference() {
        return new TypeReference<>() {};
    }
}
