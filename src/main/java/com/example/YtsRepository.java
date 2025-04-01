package com.example;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class YtsRepository {
    private final JdbcTemplate jdbcTemplate;

    public YtsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initDatabase();
    }

    private void initDatabase() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS videos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "url TEXT NOT NULL UNIQUE, " +
                        "summary TEXT, " +
                        "created_at TEXT NOT NULL" +
                        ")");
    }

    public Optional<YtsVideo> findByUrl(String url) {
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            "SELECT * FROM videos WHERE url = ?",
                            new VideoRowMapper(),
                            url
                    )
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public YtsVideo save(YtsVideo video) {
        if (video.getId() == null) {
            jdbcTemplate.update(
                    "INSERT INTO videos (url, summary, created_at) VALUES (?, ?, ?)",
                    video.getUrl(),
                    video.getSummary(),
                    video.getCreatedAt().toString()
            );
            Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
            video.setId(id);
        } else {
            jdbcTemplate.update(
                    "UPDATE videos SET url = ?, summary = ?, created_at = ? WHERE id = ?",
                    video.getUrl(),
                    video.getSummary(),
                    video.getCreatedAt().toString(),
                    video.getId()
            );
        }
        return video;
    }

    private static class VideoRowMapper implements RowMapper<YtsVideo> {
        @Override
        public YtsVideo mapRow(ResultSet rs, int rowNum) throws SQLException {
            YtsVideo video = new YtsVideo();
            video.setId(rs.getLong("id"));
            video.setUrl(rs.getString("url"));
            video.setSummary(rs.getString("summary"));
            video.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
            return video;
        }
    }
}