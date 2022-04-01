package ru.somarov.deletion_service.domain.entity.jsonb.types;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

/**
 * This class is a support for JSONB postgres type in hibernate
 *
 * @author alexandr.omarov, bajura-ea
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public abstract class PgJsonCollection<T> extends PgJsonObject {

    /**
     * The type reference of collection returned by <tt>nullSafeGet()</tt>.
     * Should be overwritten in accessors
     *
     * @return TypeReference
     */
    protected abstract TypeReference<T> getReturnedTypeReference();


    @Override
    protected Object readJson(String value) throws IOException {
        return objectMapper.readValue(value, getReturnedTypeReference());
    }


}
