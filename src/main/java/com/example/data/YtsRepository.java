package com.example.data;

import com.example.model.YtsVideo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class YtsRepository {

    // SQL statements for different database types
    private static final String CREATE_TABLE_SQLITE = """
            CREATE TABLE IF NOT EXISTS videos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                url TEXT NOT NULL UNIQUE,
                summary TEXT,
                created_at TEXT NOT NULL
            )""";

    private static final String CREATE_TABLE_H2 = """
            CREATE TABLE IF NOT EXISTS videos (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                url VARCHAR(2000) NOT NULL UNIQUE,
                summary CLOB,
                created_at VARCHAR(255) NOT NULL
            )""";

    private static final String SELECT = "SELECT * FROM videos WHERE url = ?";
    private static final String INSERT = "INSERT INTO videos (url, summary, created_at) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE videos SET url = ?, summary = ?, created_at = ? WHERE id = ?";
    private static final String LAST_ID_SQLITE = "SELECT last_insert_rowid()";
    private static final String LAST_ID_H2 = "SELECT IDENTITY()";

    private final JdbcTemplate jdbcTemplate;

    private String lastIdQuery;

    @PostConstruct
    public void initDatabase() {
        try {
            String driverName = jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverName().toLowerCase();

            if (driverName.contains("sqlite")) {
                jdbcTemplate.execute(CREATE_TABLE_SQLITE);
                lastIdQuery = LAST_ID_SQLITE;
                log.info("SQLite database initialized");
            } else {
                // Default to H2 for any other database type
                jdbcTemplate.execute(CREATE_TABLE_H2);
                lastIdQuery = LAST_ID_H2;
                log.info("H2 or other database initialized");
            }
        } catch (Exception e) {
            log.warn("Error determining database type: {}. Defaulting to H2 syntax.", e.getMessage());
            jdbcTemplate.execute(CREATE_TABLE_H2);
            lastIdQuery = LAST_ID_H2;
        }
    }

    public Optional<YtsVideo> findByUrl(String url) {
        try {
            YtsVideo video = jdbcTemplate.queryForObject(SELECT,
                    new Object[]{url},
                    videoRowMapper());
            return Optional.ofNullable(video);
        } catch (DataAccessException e) {
            log.debug("No video found for URL: {}", url);
            return Optional.empty();
        }
    }

    public YtsVideo save(YtsVideo video) {
        if (video.hasValidID()) {
            updateVideo(video);
        } else {
            insertVideo(video);
        }
        return video;
    }

    private void insertVideo(YtsVideo video) {
        int rowsAffected = jdbcTemplate.update(INSERT,
                video.getUrl(),
                video.getSummary(),
                video.getCreatedAt().toString()
        );

        if (rowsAffected > 0) {
            try {
                if (lastIdQuery != null && !lastIdQuery.isEmpty()) {
                    Long id = jdbcTemplate.queryForObject(lastIdQuery, Long.class);
                    video.setId(id);
                    log.debug("Inserted new video with ID: {}", id);
                } else {
                    // Fallback approach
                    retrieveIdByUrl(video);
                }
            } catch (Exception e) {
                log.warn("Could not retrieve generated ID: {}", e.getMessage());
                // Try to get the ID by querying the table
                retrieveIdByUrl(video);
            }
        }
    }

    // Add this helper method
    private void retrieveIdByUrl(YtsVideo video) {
        try {
            Long id = jdbcTemplate.queryForObject(
                    "SELECT id FROM videos WHERE url = ?",
                    Long.class,
                    video.getUrl()
            );
            video.setId(id);
            log.debug("Retrieved video ID by URL: {}", id);
        } catch (Exception e) {
            log.error("Failed to retrieve ID by URL: {}", e.getMessage());
        }
    }

    private void updateVideo(YtsVideo video) {
        jdbcTemplate.update(UPDATE,
                video.getUrl(),
                video.getSummary(),
                video.getCreatedAt().toString(),
                video.getId()
        );
        log.debug("Updated video with ID: {}", video.getId());
    }

    private RowMapper<YtsVideo> videoRowMapper() {
        return (rs, rowNum) -> YtsVideo.builder()
                .id(rs.getLong("id"))
                .url(rs.getString("url"))
                .summary(rs.getString("summary"))
                .createdAt(LocalDateTime.parse(rs.getString("created_at")))
                .build();
    }
}