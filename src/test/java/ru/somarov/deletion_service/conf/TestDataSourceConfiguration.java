package ru.somarov.deletion_service.conf;

import com.opentable.db.postgres.embedded.PreparedDbProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@TestConfiguration
@ConditionalOnProperty(name="app.use_embedded_postgres", havingValue = "true")
public class TestDataSourceConfiguration {

    @Bean
    DbPreparer dbPreparer() {
        return new DbPreparer();
    }

    @Bean
    public DataSource dataSource(DbPreparer dbPreparer) throws SQLException {
        log.info("********* Using Embedded Postgresql *********");
        return PreparedDbProvider.forPreparer(dbPreparer).createDataSource();
    }
}
