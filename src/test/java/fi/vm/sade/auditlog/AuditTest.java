package fi.vm.sade.auditlog;

import static fi.vm.sade.auditlog.ApplicationType.OPPIJA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private final Target target = new Target.Builder().build();
    private final AuditTestDto dto = new AuditTestDto();

    public AuditTest() throws UnknownHostException, GSSException { }

    @Before
    public void initMock() {
        when(clock.wallClockTime()).thenReturn(bootTime);
        audit = new Audit(logger, "test", OPPIJA, hostname, heartbeatDaemon, clock);
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
        audit.log(user, op, target, new Changes.Builder().build());
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(2)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(1, r.get("logSeq").getAsInt());
    }

    @Test
    public void timestamp() {
        Date now = new Date();
        when(clock.wallClockTime()).thenReturn(now);
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(now), r.get("timestamp").getAsString());
    }

    @Test
    public void bootTime() {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(bootTime), r.get("bootTime").getAsString());
    }

    @Test
    public void operation() {
        when(op.name()).thenReturn("OP");
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("OP", r.get("operation").getAsString());
    }

    @Test
    public void user() {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject user = r.getAsJsonObject("user");
        assertEquals("1.1.1.1.1.1.1", user.get("oid").getAsString());
        assertEquals("127.0.0.1", user.get("ip").getAsString());
        assertEquals("session-id", user.get("session").getAsString());
        assertEquals("user-agent", user.get("userAgent").getAsString());
    }

    @Test
    public void nullStringValue() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", (String) null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertTrue(changes.getAsJsonObject("kenttä").get("newValue").isJsonNull());
    }

    @Test
    public void nullValue() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", (JsonElement) null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertTrue(changes.getAsJsonObject("kenttä").get("newValue").isJsonNull());
    }

    @Test
    public void withChange() {
        audit.log(user, op, target, new Changes.Builder().updated("kenttä", "vanhaArvo", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("vanhaArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue").getAsString());
        assertEquals("uusiArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue").getAsString());
    }

    @Test
    public void withAddedString() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("uusiArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue"));
    }

    @Test
    public void withAddedObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("nestedKey", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added("kenttä", newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("uusiArvo", changes
                .getAsJsonObject("kenttä")
                .getAsJsonObject("newValue")
                .getAsJsonPrimitive("nestedKey").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue"));
    }

    @Test
    public void withRemoved() {
        audit.log(user, op, target, new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject changes = r.getAsJsonObject("changes");
        assertEquals("vanhaArvo", changes.getAsJsonObject("kenttä").getAsJsonPrimitive("oldValue").getAsString());
        assertNull(changes.getAsJsonObject("kenttä").getAsJsonPrimitive("newValue"));
    }

    @Test
    public void withTarget() {
        audit.log(user, op,
                new Target.Builder().setField("henkilö", "person-oid").setField("hakemus", "hakemus-oid").build(),
                new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonObject target = r.getAsJsonObject("target");
        assertEquals("person-oid", target.getAsJsonPrimitive("henkilö").getAsString());
        assertEquals("hakemus-oid", target.getAsJsonPrimitive("hakemus").getAsString());
    }

    @Test
    public void logType() {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("log", r.get("type").getAsString());
    }

    @Test
    public void aliveType() {
        audit.logHeartbeat();
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("alive", r.get("type").getAsString());
    }

    @Test
    public void truncatesLongField() {
        JsonElement json = gson.toJsonTree(dto);
        assert(json.toString().length() > Audit.MAX_FIELD_LENGTH);

        Util.traverseAndTruncate(json);

        String truncatedString = json.getAsJsonObject().get("longString").getAsString();
        assertTrue(truncatedString.length() < dto.longString.length());
        assertTrue(truncatedString.length() < Audit.MAX_FIELD_LENGTH);
        assertTrue(json.toString().length() < Audit.MAX_FIELD_LENGTH);
    }

    @Test
    public void truncatesLongArrayElement() {
        JsonElement json = gson.toJsonTree(dto);
        assertTrue(json.toString().length() > Audit.MAX_FIELD_LENGTH);

        Util.traverseAndTruncate(json);

        String truncatedString = json.getAsJsonObject().get("array").getAsJsonArray().get(0).getAsString();
        assertTrue(truncatedString.length() < dto.longString.length());
        assertTrue(truncatedString.length() < Audit.MAX_FIELD_LENGTH);
        assertTrue(json.toString().length() < Audit.MAX_FIELD_LENGTH);
    }

    @Test
    public void truncatedStringsMatchForIdenticalInputs() {
        JsonElement json = gson.toJsonTree(dto);
        Util.traverseAndTruncate(json);

        String truncatedString1 = json.getAsJsonObject().get("longString").getAsString();
        String truncatedString2 = json.getAsJsonObject().get("array").getAsJsonArray().get(0).getAsString();
        assertEquals(truncatedString1, truncatedString2);
    }

    @Test
    public void doesNotTruncateShortField() {
        JsonElement json = gson.toJsonTree(dto);
        Util.traverseAndTruncate(json);

        String shortString = json.getAsJsonObject().get("shortString").getAsString();
        assertEquals("bee", shortString);
    }

    @Test
    public void doesNotTruncateNumber() {
        JsonElement json = gson.toJsonTree(dto);
        Util.traverseAndTruncate(json);

        int number = json.getAsJsonObject().get("number").getAsInt();
        assertEquals(99, number);
    }

    @Test
    public void testJsonPatchDiff() {
        AuditTestDto changedDto = new AuditTestDto();
        changedDto.shortString = "wasp";

        JsonElement json = gson.toJsonTree(dto);
        JsonElement jsonChanged = gson.toJsonTree(changedDto);
        Changes.Builder builder = new Changes.Builder();
        Util.jsonDiffToChanges(builder, json, jsonChanged);
        Changes build = builder.build();

        JsonObject jsonObject = build.asJson();
        assertEquals(jsonObject.get("shortString").getAsJsonObject().get("oldValue").getAsString(), "bee");
        assertEquals(jsonObject.get("shortString").getAsJsonObject().get("newValue").getAsString(), "wasp");
    }

    private static String createLongString() {
        int length = 33000;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }

    public static class AuditTestDto {
        public String longString = createLongString();
        public String shortString = "bee";
        public int number = 99;
        public String[] array = new String[] { createLongString() };
    }
}
