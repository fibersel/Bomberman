package bomberman.matchmaker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Configuration
public class MatchMakerTestConfig {

    @Bean
    public JdbcTemplate getJdbcTemplate() {
        DataSource db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .addScript("monitoring_test.sql")
                .build();
        return new JdbcTemplate(db);
    }


    @Bean
    public Map<Long,AtomicInteger> getMap() {
        return new ConcurrentHashMap<>();
    }

}
