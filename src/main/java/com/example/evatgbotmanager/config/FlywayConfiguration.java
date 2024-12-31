package com.example.evatgbotmanager.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfiguration {

    @Bean(initMethod = "migrate")
    public Flyway flyway(@Value("${spring.flyway.url}") String url,
                         @Value("${spring.flyway.user}") String user,
                         @Value("${spring.flyway.password}") String password,
                         @Value("${spring.flyway.baseline-on-migrate}") boolean baselineOnMigrate){
        return new Flyway(Flyway.configure().dataSource(url, user, password).baselineOnMigrate(baselineOnMigrate));
    }
}
