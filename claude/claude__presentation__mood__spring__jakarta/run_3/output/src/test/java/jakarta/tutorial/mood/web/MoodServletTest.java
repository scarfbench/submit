package jakarta.tutorial.mood.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoodServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private MoodServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new MoodServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testGetReport() throws Exception {
        when(request.getAttribute("mood")).thenReturn("awake");
        when(request.getParameter("name")).thenReturn("Duke");

        servlet.doGet(request, response);

        writer.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("current mood"));
        assertTrue(output.contains("Duke"));
        assertTrue(output.contains("awake"));
        assertTrue(output.contains("duke.waving.gif"));
        verify(response).setContentType("text/html;charset=UTF-8");
    }

    @Test
    void testGetReportWithDefaultName() throws Exception {
        when(request.getAttribute("mood")).thenReturn("happy");
        when(request.getParameter("name")).thenReturn(null);

        servlet.doGet(request, response);

        writer.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("friend"));
        assertTrue(output.contains("happy"));
    }
}
