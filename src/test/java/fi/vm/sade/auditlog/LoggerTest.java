package fi.vm.sade.auditlog;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LoggerTest {
    private Audit audit;
    private Logger loggerMock;

    @Before
    public void initMock() {
        loggerMock = mock(Logger.class);
        audit = new Audit("test", loggerMock);
    }

    @Test
    public void smokeTest() {
        audit.log("test message");
        verify(loggerMock).info(anyString());
    }

    @Test
    public void writeTest() {
        String filename = "./log.txt";
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

        Audit audit = new Audit("TEST", filename);
        audit.log("Testi viesti");

        assertTrue(file.exists());
        assertEquals(file.length(), 80);
        file.delete();
        assertFalse(file.exists());
    }
}
