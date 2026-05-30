package dev.salim.backend.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DatabaseSchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("alter table if exists tours add column if not exists route_stops varchar(2000)");
            jdbcTemplate.execute("alter table if exists tours alter column route_geo_json type text");
            jdbcTemplate.execute("update tours set transport_type = 'CAR' where transport_type in ('TRAIN', 'PLANE')");
            log.info("Ensured route stop, transport type and OpenRouteService geometry columns are compatible");
        } catch (DataAccessException ex) {
            log.warn("Could not adjust tours.route_geo_json column: {}", ex.getMostSpecificCause().getMessage());
        }
    }
}
