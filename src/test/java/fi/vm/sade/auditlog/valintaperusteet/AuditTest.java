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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditTest {

    private Gson gson = new Gson();
    private Audit audit;
    @Mock private Logger logger;
    @Mock private HeartbeatDaemon heartbeatDaemon;
    @Mock private Clock clock;
    @Mock private Operation op;
    @Captor private ArgumentCaptor<String> msgCaptor;

    private final Date bootTime = new Date();
    private final String hostname = "hostname";
    private final User user = new User(
            new Oid("1.1.1.1.1.1.1"),
            InetAddress.getByName("127.0.0.1"),
            "session-id",
            "user-agent");

    public AuditTest() throws UnknownHostException, GSSException { }

    @Before
    public void initMock() {
        when(clock.wallClockTime()).thenReturn(bootTime);
        audit = new Audit(logger, "test", ApplicationType.OPISKELIJA, hostname, heartbeatDaemon, clock);
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
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().build());
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().build());
        verify(logger, times(2)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(1, r.get("logSeq").getAsInt());
    }

    @Test
    public void timestamp() {
        Date now = new Date();
        when(clock.wallClockTime()).thenReturn(now);
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(now), r.get("timestamp").getAsString());
    }

    @Test
    public void bootTime() {
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(bootTime), r.get("bootTime").getAsString());
    }

    @Test
    public void operation() {
        when(op.name()).thenReturn("OP");
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("OP", r.get("operation").getAsString());
    }

    @Test
    public void user() {
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject user = r.getAsJsonObject("user");
        assertEquals("1.1.1.1.1.1.1", user.get("oid").getAsString());
        assertEquals("127.0.0.1", user.get("ip").getAsString());
        assertEquals("session-id", user.get("session").getAsString());
        assertEquals("user-agent", user.get("userAgent").getAsString());
    }

    @Test
    public void nullValue() {
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().added("kenttä", null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertTrue(changes.getAsJsonObject("kenttä").get("newValue").isJsonNull());
    }

    @Test
    public void withChange() throws UnknownHostException {
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().updated("kenttä", "vanhaArvo", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("vanhaArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue").getAsString());
        assertEquals("uusiArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue").getAsString());
    }

    @Test
    public void withAdded() throws UnknownHostException {
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().added("kenttä", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("uusiArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue"));
    }

    @Test
    public void withRemoved() throws UnknownHostException {
        audit.log(user, op, new Target.Builder().build(), new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("vanhaArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue"));
    }

    @Test
    public void withTarget() throws UnknownHostException {
        audit.log(user, op,
                new Target.Builder().setField("henkilö", "person-oid").setField("hakemus", "hakemus-oid").build(),
                new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject target = r.getAsJsonObject("target");
        assertEquals("person-oid", target.getAsJsonPrimitive("henkilö").getAsString());
        assertEquals("hakemus-oid", target.getAsJsonPrimitive("hakemus").getAsString());
    }
}
