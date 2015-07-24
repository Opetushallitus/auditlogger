package fi.vm.sade.auditlog;

import org.slf4j.Logger;
import org.junit.Before;
import org.junit.Test;

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

}
