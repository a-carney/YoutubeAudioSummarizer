package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class YtsRepositoryTest {

    @Mock
    private JdbcTemplate mockJdbcTemplate;

    private YtsRepository ytsRepository;

    @BeforeEach
    void setUp() {
        ytsRepository = new YtsRepository(mockJdbcTemplate);
    }

    @Test
    void testInitDatabase() {
        verify(mockJdbcTemplate).execute("CREATE TABLE IF NOT EXISTS " +
                "videos (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "url TEXT NOT NULL UNIQUE, " +
                "summary TEXT, " +
                "created_at TEXT NOT NULL)");
    }


    @Test
    void testSave_NewVideo() {
        YtsVideo newVideo = new YtsVideo("https://www.youtube.com/watch?v=test");
        newVideo.setSummary("Test summary");

        when(mockJdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class)).thenReturn(1L);

        YtsVideo savedVideo = ytsRepository.save(newVideo);

        assertNotNull(savedVideo.getId());
        assertEquals(1L, savedVideo.getId().longValue());

        verify(mockJdbcTemplate).update(
                "INSERT INTO videos (url, summary, created_at) VALUES (?, ?, ?)",
                newVideo.getUrl(),
                newVideo.getSummary(),
                newVideo.getCreatedAt().toString()
        );
    }

    @Test
    void testSave_ExistingVideo() {
        YtsVideo existingVideo = new YtsVideo("https://www.youtube.com/watch?v=test");
        existingVideo.setId(1L);
        existingVideo.setSummary("Updated summary");

        YtsVideo savedVideo = ytsRepository.save(existingVideo);

        assertEquals(existingVideo, savedVideo);

        verify(mockJdbcTemplate).update(
                "UPDATE videos SET url = ?, summary = ?, created_at = ? WHERE id = ?",
                existingVideo.getUrl(),
                existingVideo.getSummary(),
                existingVideo.getCreatedAt().toString(),
                existingVideo.getId()
        );
    }
}
