package com.example;

import com.example.common.YtsUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class YtsUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "http://youtu.be/dQw4w9WgXcQ"
    })
    void isValidURL_withValidUrls_returnsTrue(String validUrl) {
        assertTrue(YtsUtil.isValidURL(validUrl));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "https://example.com",
            "not-a-url"
    })
    void isValidURL_withInvalidUrls_returnsFalse(String invalidUrl) {
        assertFalse(YtsUtil.isValidURL(invalidUrl));
    }

    @Test
    void extractVideoId_withValidUrl_returnsVideoId() {
        // Arrange
        String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String expectedId = "dQw4w9WgXcQ";

        // Act
        String actualId = YtsUtil.extractVideoId(url);

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    void extractVideoId_withShortUrl_returnsVideoId() {
        // Arrange
        String url = "https://youtu.be/dQw4w9WgXcQ";
        String expectedId = "dQw4w9WgXcQ";

        // Act
        String actualId = YtsUtil.extractVideoId(url);

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    void extractVideoId_withInvalidUrl_returnsNull() {
        assertNull(YtsUtil.extractVideoId("https://example.com"));
    }

    @Test
    void execute_withSimpleCommand_returnsExitCode() {
        // We'll use a simple command that's available on most systems
        int result = YtsUtil.execute("echo test");
        assertEquals(0, result);
    }
}
