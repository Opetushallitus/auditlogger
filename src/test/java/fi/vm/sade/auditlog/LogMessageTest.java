package fi.vm.sade.auditlog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogMessageTest {

    @Test
    public void toStringFormatsLogMessage() {
        LogMessage logMessage = new LogMessage("ID")
                .withPalvelunTunniste("omatsivut")
                .withTunniste("opiskelija")
                .withLokiviesti("Opiskelija kirjautui sisään");
        assertEquals("id='ID', palvelunTunniste='OMATSIVUT', tunniste='OPISKELIJA', lokiviesti='Opiskelija kirjautui sisään'", logMessage.toString());
    }
}
