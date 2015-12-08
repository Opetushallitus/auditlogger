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
import java.util.Map;

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
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Before
    public void initMock() {
        loggerMock = mock(Logger.class);
        audit = new Audit(loggerMock, "test", ApplicationType.OPISKELIJA);
    }

    @Test
    public void smokeTest() throws ParseException {
        Date now = new SimpleDateFormat("yyyy-MM-dd").parse("2015-12-01");
        audit.log(builder().id("testuser").timestamp(now).add("tila", TESTENUM.TILA1, TESTENUM.TILA2).message("test message").build());
        ArgumentCaptor<String> infoCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).info(infoCapture.capture());
        final String logMessage = infoCapture.getValue();
        assertEquals("{\"timestamp\":\"2015-12-01 00:00:00.000\",\"serviceName\":\"test\",\"applicationType\":\"opiskelija\",\"tila.old_value\":\"TILA2\",\"id\":\"testuser\",\"tila\":\"TILA1\",\"message\":\"test message\"}", logMessage);
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

    private Map<String,String> jsonToMap(String jsonString) {
        return new Gson().fromJson(jsonString, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
