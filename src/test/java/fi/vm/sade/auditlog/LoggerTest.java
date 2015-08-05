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
        audit = new Audit(loggerMock, "test", ApplicationType.OPISKELIJA);
    }

    @Test
    public void smokeTest() {
        audit.log(new LogMessage("ID", "opiskelija", "test message"));
        verify(loggerMock).info(eq("opiskelija-app: [test] id='ID', userIdentity='opiskelija', message='test message'"));
    }

    @Test
    public void writeTest() {
        File file = new File("auditlog_test.log");
        if (file.exists()) {
            file.delete();
        }

        Audit audit = new Audit("TEST", ApplicationType.VIRKAILIJA);
        audit.log("Testi viesti");
        LogMessage logMessage = new LogMessage("ID", "virkailija", "Virkailija kirjautui sisään");
        audit.log(logMessage);

        assertTrue(file.exists());
        assertTrue(file.length() > 160);
        file.delete();
        assertFalse(file.exists());
    }
}
