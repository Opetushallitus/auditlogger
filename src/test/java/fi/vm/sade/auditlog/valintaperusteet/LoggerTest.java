package fi.vm.sade.auditlog.valintaperusteet;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.valintaperusteet.LogMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static fi.vm.sade.auditlog.valintaperusteet.LogMessage.builder;

public class LoggerTest {
    private enum TESTENUM {
        TILA1, TILA2
    }
    private Audit audit;
    private Logger loggerMock;
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    @Before
    public void initMock() {
        loggerMock = mock(Logger.class);
        audit = new Audit(loggerMock, "test", ApplicationType.OPISKELIJA);
    }

    @Test
    public void smokeTest() {
        Date now = new Date();
        audit.log(builder()
                .id("testuser")
                .timestamp(now)
                .add("tila", TESTENUM.TILA1, TESTENUM.TILA2)
                .message("test message")
                .build());
        ArgumentCaptor<String> infoCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).info(infoCapture.capture());
        Assert.assertEquals(jsonToMap(infoCapture.getValue()), ImmutableMap.builder()
                .put("id","testuser")
                .put("timestamp",SDF.format(now))
                .put("message","test message")
                .put("tila.old_value","TILA2")
                .put("tila","TILA1")
                .put("serviceName","test")
                .put("applicationType","opiskelija").build());
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
