package com.example;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class YtsRepositoryTest {

    @Mock
    private JdbcTemplate mockJdbcTemplate;

    private YtsRepository ytsRepository;

    @Before
    public void setUp() {
        ytsRepository = new YtsRepository(mockJdbcTemplate);
    }

    @Test
    public void testInitDatabase() {
        // Verify that the database initialization SQL is executed
        verify(mockJdbcTemplate).execute(
                "CREATE TABLE IF NOT EXISTS videos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "url TEXT NOT NULL UNIQUE, " +
                        "summary TEXT, " +
                        "created_at TEXT NOT NULL" +
                        ")"
        );
    }


    @Test
    public void testSave_NewVideo() {
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
    public void testSave_ExistingVideo() {
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
