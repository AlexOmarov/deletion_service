package ru.somarov.deletion_service.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

import static net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock.InterceptMode.PROXY_SCHEDULER;

/**
 * This class is a scheduling config.
 * Responsible for enabling schedulers all across application  by `app.scheduling.enabled` property
 * Also controls parallel execution of scheduled tasks via shedlock library, which writes lock into db table
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 *
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${app.schedlock.scheduler.default.max.lock}",
        defaultLockAtLeastFor = "${app.schedlock.scheduler.default.min.lock}", interceptMode = PROXY_SCHEDULER)
@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true")
public class SchedulingConfig {

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName(schema + ".shedlock")
                        .usingDbTime()
                        .build()
        );
    }
}
