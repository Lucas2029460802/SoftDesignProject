package com.editor.logging;

import com.editor.observer.Event;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Logger测试类
 */
public class LoggerTest {
    private Logger logger;
    private Path testFile;

    @Before
    public void setUp() throws Exception {
        logger = new Logger();
        testFile = Files.createTempFile("test", ".txt");
    }

    @Test
    public void testEnableDisableLog() {
        String filePath = testFile.toString();
        assertFalse(logger.isLogEnabled(filePath));
        logger.enableLog(filePath);
        assertTrue(logger.isLogEnabled(filePath));
        logger.disableLog(filePath);
        assertFalse(logger.isLogEnabled(filePath));
    }

    @Test
    public void testLogCommand() {
        String filePath = testFile.toString();
        logger.enableLog(filePath);
        logger.logCommand(filePath, "test command");
        String logContent = logger.readLog(filePath);
        assertTrue(logContent.contains("test command"));
    }

    @Test
    public void testObserverUpdate() {
        String filePath = testFile.toString();
        logger.enableLog(filePath);
        Event event = new Event("EDIT", "test command", filePath);
        logger.update(event);
        String logContent = logger.readLog(filePath);
        assertTrue(logContent.contains("test command") || logContent.contains("session start"));
    }
}

