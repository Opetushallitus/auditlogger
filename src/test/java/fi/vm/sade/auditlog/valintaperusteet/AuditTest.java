package fi.vm.sade.auditlog.valintaperusteet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.auditlog.Changes;
import fi.vm.sade.auditlog.Clock;
import fi.vm.sade.auditlog.HeartbeatDaemon;
import fi.vm.sade.auditlog.Logger;
import fi.vm.sade.auditlog.Operation;
import fi.vm.sade.auditlog.Target;
import fi.vm.sade.auditlog.User;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuditTest {

    private Gson gson = new Gson();
    private Audit audit;
    private Logger loggerMock;
    private HeartbeatDaemon heartbeatDaemon;
    private Clock clock;
    private final String hostname = "hostname";
    private final Operation op = new Operation() {
        @Override
        public String name() {
            return "OP";
        }
    };
    private final User user = new User(
            new Oid("1.1.1.1.1.1.1"),
            InetAddress.getByName("127.0.0.1"),
            "session-id",
            "user-agent");

    public AuditTest() throws UnknownHostException, GSSException { }

    @Before
    public void initMock() {
        loggerMock = mock(Logger.class);
        heartbeatDaemon = mock(HeartbeatDaemon.class);
        clock = mock(Clock.class);
        when(clock.wallClockTime()).thenReturn(date("2015-12-01 00:30+02:00"));
        audit = new Audit(loggerMock, "test", ApplicationType.OPISKELIJA, hostname, heartbeatDaemon, clock);
    }

    @Test
    public void write() {
        File file = new File("auditlog_test.log");
        if (file.exists()) {
            file.delete();
        }

        final org.slf4j.Logger logbackLogger = LoggerFactory.getLogger(Audit.class);
        Logger logger = new Logger() {
            @Override
            public void log(String msg) {
                logbackLogger.info(msg);
            }
        };
        Audit audit = new Audit(logger, "TEST", ApplicationType.VIRKAILIJA, hostname, heartbeatDaemon, clock);
        audit.log(user, op, new Target(), new Changes());

        assertTrue(file.exists());
        assertTrue(file.length() > 100);
        file.delete();
    }

    @Test
    public void heartbeat() {
        Audit mockAudit = mock(Audit.class);
        HeartbeatDaemon heartbeat = HeartbeatDaemon.getInstance();
        heartbeat.register(mockAudit);
        heartbeat.run();
        heartbeat.shutdown();
        verify(mockAudit, times(1)).logStarted();
        verify(mockAudit, times(1)).logHeartbeat();
        verify(mockAudit, times(1)).logStopped();
    }

    @Test
    public void sequence() {
        audit.log(user, op, new Target(), new Changes());
        audit.log(user, op, new Target(), new Changes());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(2)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        assertEquals(1, r.get("logSeq").getAsInt());
    }

    @Test
    public void timestamp() {
        when(clock.wallClockTime()).thenReturn(date("2015-12-01 15:30+02:00"));
        audit.log(user, op, new Target(), new Changes());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        assertEquals("2015-12-01T15:30:00.000+02", r.get("timestamp").getAsString());
    }

    @Test
    public void bootTime() {
        audit.log(user, op, new Target(), new Changes());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        assertEquals("2015-12-01T00:30:00.000+02", r.get("bootTime").getAsString());
    }

    @Test
    public void operation() {
        audit.log(user, op, new Target(), new Changes());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        assertEquals("OP", r.get("operation").getAsString());
    }

    @Test
    public void user() {
        audit.log(user, op, new Target(), new Changes());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        JsonObject user = r.getAsJsonObject("user");
        assertEquals("1.1.1.1.1.1.1", user.get("oid").getAsString());
        assertEquals("127.0.0.1", user.get("ip").getAsString());
        assertEquals("session-id", user.get("session").getAsString());
        assertEquals("user-agent", user.get("userAgent").getAsString());
    }

    @Test
    public void nullValue() {
        audit.log(user, op, new Target(), new Changes.Builder().added("kenttä", null).build());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertTrue(changes.getAsJsonObject("kenttä").get("newValue").isJsonNull());
    }

    @Test
    public void withChange() throws UnknownHostException {
        audit.log(user, op, new Target(), new Changes.Builder().updated("kenttä", "vanhaArvo", "uusiArvo").build());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("vanhaArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue").getAsString());
        assertEquals("uusiArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue").getAsString());
    }

    @Test
    public void withAdded() throws UnknownHostException {
        audit.log(user, op, new Target(), new Changes.Builder().added("kenttä", "uusiArvo").build());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("uusiArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue"));
    }

    @Test
    public void withRemoved() throws UnknownHostException {
        audit.log(user, op, new Target(), new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("vanhaArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue"));
    }

    @Test
    public void withTarget() throws UnknownHostException {
        audit.log(user, op,
                new Target.Builder().setField("henkilö", "person-oid").setField("hakemus", "hakemus-oid").build(),
                new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        ArgumentCaptor<String> msgCapture = ArgumentCaptor.forClass(String.class);
        verify(loggerMock, times(1)).log(msgCapture.capture());
        JsonObject r = gson.fromJson(msgCapture.getValue(), JsonObject.class);
        JsonObject target = r.getAsJsonObject("target");
        assertEquals("person-oid", target.getAsJsonPrimitive("henkilö").getAsString());
        assertEquals("hakemus-oid", target.getAsJsonPrimitive("hakemus").getAsString());
    }

    public final static Date date(String string) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mmX").parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
