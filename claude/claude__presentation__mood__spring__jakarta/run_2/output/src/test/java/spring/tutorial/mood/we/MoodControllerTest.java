// src/test/java/spring/tutorial/mood/web/MoodControllerTest.java
package spring.tutorial.mood.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class MoodControllerTest {

    @Test
    void testGetReportWithName() {
        MoodController controller = new MoodController();
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getAttribute("mood")).thenReturn("awake");

        String result = controller.getReport(mockRequest, "Duke");

        assertNotNull(result);
        assertTrue(result.contains("Duke"));
        assertTrue(result.contains("awake"));
        assertTrue(result.contains("current mood"));
        assertTrue(result.contains("/images/duke.waving.gif"));
    }

    @Test
    void testGetReportWithoutName() {
        MoodController controller = new MoodController();
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getAttribute("mood")).thenReturn("awake");

        String result = controller.getReport(mockRequest, "");

        assertNotNull(result);
        assertTrue(result.contains("friend"));
        assertTrue(result.contains("awake"));
    }

    @Test
    void testPostReport() {
        MoodController controller = new MoodController();
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getAttribute("mood")).thenReturn("happy");

        String result = controller.postReport(mockRequest, "Test");

        assertNotNull(result);
        assertTrue(result.contains("Test"));
        assertTrue(result.contains("happy"));
    }
}
