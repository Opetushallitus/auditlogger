package fi.vm.sade.auditlog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogMessageTest {

    @Test
    public void toStringFormatsLogMessage() {
        LogMessage logMessage = new LogMessage("ID", "Opiskelija kirjautui sisään");
        assertEquals("id='ID', message='Opiskelija kirjautui sisään'", logMessage.toString());
    }
}
