package com.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@TestConfiguration
public class TestConfig {


    public static final String VID = "jNQXAC9IVRw";
    public static final String FAKE_SUMMARY = "Fake Summary";
    public static final String FAKE_URL = "https://www.youtube.com/watch?v=test";
    public static final String INVALID_URL = "invalid-link";

    private static final String DS_CLASS = "org.sqlite.JDBC";
    private static final String DS_URL = "jdbc:sqlite:file::memory:?cache=shared";

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(DS_CLASS);
        ds.setUrl(DS_URL);

        return ds;
    }
}
