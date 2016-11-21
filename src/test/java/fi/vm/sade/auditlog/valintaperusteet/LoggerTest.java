package fi.vm.sade.auditlog.valintaperusteet;

import static fi.vm.sade.auditlog.valintaperusteet.LogMessage.builder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.vm.sade.auditlog.HeartbeatDaemon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;

public class LoggerTest {
    private enum TESTENUM {
        TILA1, TILA2
    }
    private Audit audit;
    private Logger loggerMock;
    private HeartbeatDaemon heartbeatDaemon;

    @Before
    public void initMock() {
        loggerMock = mock(Logger.class);
        heartbeatDaemon = mock(HeartbeatDaemon.class);
        audit = new Audit(loggerMock, "test", ApplicationType.OPISKELIJA, heartbeatDaemon);
    }

    @Test
    public void testLogMessageFormat() throws ParseException {
        final LogMessage.LogMessageBuilder messageBuilder = builder().id("testuser").add("tila", TESTENUM.TILA1, TESTENUM.TILA2).message("test message");
        verifyLogMessage(messageBuilder, "{\"logSeq\":0,\"bootTime\":null,\"timestamp\":\"2015-12-01T15:30:00.000+02\",\"serviceName\":\"test\",\"applicationType\":\"opiskelija\",\"tila.old_value\":\"TILA2\",\"id\":\"testuser\",\"tila\":\"TILA1\",\"message\":\"test message\"}");
    }

    @Test
    public void testJsonEncoding() throws ParseException {
        final LogMessage.LogMessageBuilder messageBuilder = builder().id("testuser").add("tila", TESTENUM.TILA1, TESTENUM.TILA2).message("test \" message");
        verifyLogMessage(messageBuilder, "{\"logSeq\":0,\"bootTime\":null,\"timestamp\":\"2015-12-01T15:30:00.000+02\",\"serviceName\":\"test\",\"applicationType\":\"opiskelija\",\"tila.old_value\":\"TILA2\",\"id\":\"testuser\",\"tila\":\"TILA1\",\"message\":\"test \\\" message\"}");
    }

    private void verifyLogMessage(final LogMessage.LogMessageBuilder msg, final String expectedMessage) {
        Date now = date("2015-12-01 15:30+02:00");
        audit.log(msg.timestamp(now).build());
        ArgumentCaptor<String> infoCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).info(infoCapture.capture());
        final String logMessage = infoCapture.getValue();
        assertEquals(jsonToMap(expectedMessage), jsonToMap(logMessage));
    }

    @Test
    public void writeTest() {
        File file = new File("auditlog_test.log");
        if (file.exists()) {
            file.delete();
        }

        Audit audit = new Audit("TEST", ApplicationType.VIRKAILIJA);
        LogMessage logMessage =
                builder().id("ID").message("Virkailija kirjautui sisään").build();
        audit.log(logMessage);

        assertTrue(file.exists());
        assertTrue(file.length() > 100);
        file.delete();
        assertFalse(file.exists());
    }

    @Test
    public void nullValueTest() throws ParseException {
        final LogMessage.LogMessageBuilder messageBuilder = builder().id(null).add("tila", null, TESTENUM.TILA2).message(null);
        verifyLogMessage(messageBuilder, "{\"logSeq\":0,\"bootTime\":null,\"timestamp\":\"2015-12-01T15:30:00.000+02\",\"serviceName\":\"test\",\"applicationType\":\"opiskelija\",\"tila" +
                ".old_value\":\"TILA2\",\"id\":null,\"tila\":null,\"message\":null}");
    }

    @Test
    public void nullValueTestDynamic() {
        Map<String,String> map = new HashMap<>();
        map.put("nullValue", null);
        final LogMessage.LogMessageBuilder messageBuilder = builder().addAll(map);
        verifyLogMessage(messageBuilder, "{\"logSeq\":0,\"bootTime\":null,\"timestamp\":\"2015-12-01T15:30:00.000+02\",\"serviceName\"=\"test\",\"applicationType\"=\"opiskelija\",\"nullValue\":null}");
    }

    private Map<String,String> jsonToMap(String jsonString) {
        return new Gson().fromJson(jsonString, new TypeToken<Map<String, String>>() {}.getType());
    }

    public final static Date date(String string) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mmX").parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
