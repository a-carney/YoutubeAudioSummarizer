package com.example;

import com.example.data.YtsRepository;
import com.example.model.YtsVideo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for YtsRepository using H2 in-memory database
 */
@JdbcTest
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class YtsRepositoryTest {

    private YtsRepository repository;
    private JdbcTemplate jdbcTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public YtsRepository ytsRepository(JdbcTemplate jdbcTemplate) {
            return new YtsRepository(jdbcTemplate);
        }
    }

    @BeforeEach
    void setUp() {
        DataSource dataSource = new TestConfig().dataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new YtsRepository(jdbcTemplate);

        // Create the table with H2 compatible syntax
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS videos (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    url VARCHAR(255) NOT NULL UNIQUE,
                    summary CLOB,
                    created_at VARCHAR(255) NOT NULL
                )""");
    }

    @AfterEach
    void tearDown() {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS videos");
        } catch (Exception ex) {
            System.err.println("cannot perform tearDown()");
        }
    }

    @Test
    void findByUrl_whenNoVideoExists_returnsEmptyOptional() {
        // Act
        Optional<YtsVideo> result = repository.findByUrl("https://www.youtube.com/watch?v=nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void save_newVideo_insertsAndReturnsWithId() {
        // Arrange
        String url = "https://www.youtube.com/watch?v=test123";
        String summary = "Test summary";
        YtsVideo video = new YtsVideo(url);
        video.setSummary(summary);

        // Initialize ID sequence in H2
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS SYSTEM_SEQUENCE_FOR_ID_GEN START WITH 1 INCREMENT BY 1");

        // Act
        YtsVideo savedVideo = repository.save(video);

        // Assert
        assertTrue(savedVideo.hasValidID());
        assertEquals(url, savedVideo.getUrl());
        assertEquals(summary, savedVideo.getSummary());

        // Verify it can be retrieved
        Optional<YtsVideo> retrieved = repository.findByUrl(url);
        assertTrue(retrieved.isPresent());
        assertEquals(savedVideo.getId(), retrieved.get().getId());
    }

    @Test
    void save_existingVideo_updatesRecord() {
        // Arrange
        String url = "https://www.youtube.com/watch?v=test456";  // Changed URL to avoid conflicts
        YtsVideo video = new YtsVideo(url);
        video.setSummary("Initial summary");

        // Initialize ID sequence in H2
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS SYSTEM_SEQUENCE_FOR_ID_GEN START WITH 1 INCREMENT BY 1");

        // First save the video using the repository to create the initial record
        YtsVideo savedVideo = repository.save(video);
        assertNotNull(savedVideo.getId(), "The saved video should have an ID");

        // Update summary
        String updatedSummary = "Updated summary";
        savedVideo.setSummary(updatedSummary);

        // Act
        YtsVideo updatedVideo = repository.save(savedVideo);

        // Assert
        assertEquals(savedVideo.getId(), updatedVideo.getId());
        assertEquals(updatedSummary, updatedVideo.getSummary());

        // Verify from db
        Optional<YtsVideo> retrieved = repository.findByUrl(url);
        assertTrue(retrieved.isPresent());
        assertEquals(updatedSummary, retrieved.get().getSummary());
    }
}