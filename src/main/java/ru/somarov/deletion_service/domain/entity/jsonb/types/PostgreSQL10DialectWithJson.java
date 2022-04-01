package ru.somarov.deletion_service.domain.entity.jsonb.types;

import org.hibernate.dialect.PostgreSQL10Dialect;

import java.sql.Types;

/**
 * This class is a config for hibernate postgres dialect supporting json data types
 *
 * @author alexandr.omarov, bajura-ea
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public class PostgreSQL10DialectWithJson extends PostgreSQL10Dialect {

    public PostgreSQL10DialectWithJson() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
