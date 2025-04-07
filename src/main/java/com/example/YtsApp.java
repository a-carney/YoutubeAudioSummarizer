package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Slf4j
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})

public class YtsApp {
    private static final String SQLITE = "org.sqlite.JDBC";
    private static final String DB_FILE = "video-summaries.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static final String MODE = "app.mode";
    private static final String SHELL = "shell";
    private static final String WEB = "web";

    private static final String INTERACTIVE_SHELL = "spring.shell.interactive.enabled";
    private static final String TRUE = "true";
    private static final String FALSE = "false";



    public static void main(String[] args) {
        if (isTerminalMode(args)) {
            System.setProperty(MODE, SHELL);
            System.setProperty(INTERACTIVE_SHELL, TRUE);
        }

        if (isWebMode()) {
            System.setProperty(MODE, WEB);
            System.setProperty(INTERACTIVE_SHELL, FALSE);
        }

        SpringApplication.run(YtsApp.class, args);
        log.info("Youtube Summarizer Initialized in {} mode", System.getProperty(MODE));

    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(SQLITE);
        ds.setUrl(DB_URL);

        log.info("SQLITE DB configured at: {}", DB_URL);

        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource source) {
        return new JdbcTemplate(source);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnProperty(name ="app.mode", havingValue = "web", matchIfMissing = true)
    public ApplicationRunner disableShell() {
        return args -> {
            log.info("WEB mode is active");
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.mode", havingValue = "shell")
    public CommandLineRunner enableShell() {
        return args -> {
            log.info("SHELL mode is active");
        };
    }

    private static boolean isTerminalMode(String[] args) {
        for (String arg : args) {
            if (arg.equals("--c") || arg.equals("--comand-line")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWebMode() {
       return (System.getProperty(MODE) == null);
    }

}
