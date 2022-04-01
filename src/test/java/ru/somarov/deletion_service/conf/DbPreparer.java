package ru.somarov.deletion_service.conf;

import com.opentable.db.postgres.embedded.DatabasePreparer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DbPreparer implements DatabasePreparer {

    private static final String DROP_SCHEMA_SQL = "DROP SCHEMA IF EXISTS %s CASCADE";

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    @Override
    public void prepare(DataSource ds) throws SQLException {
        try(Connection connection = ds.getConnection()) {
            connection.prepareStatement(String.format(DROP_SCHEMA_SQL, schema)).execute();
        }
    }

}
