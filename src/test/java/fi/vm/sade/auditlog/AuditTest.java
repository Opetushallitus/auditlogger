package fi.vm.sade.auditlog;

import static fi.vm.sade.auditlog.ApplicationType.OPPIJA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.*;

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
    private AuditTestDto dto = new AuditTestDto();
    private AuditTestDtoWithNumberString dtoWithNumberString = new AuditTestDtoWithNumberString();

    public AuditTest() throws UnknownHostException, GSSException { }

    @Before
    public void initMock() {
        when(clock.wallClockTime()).thenReturn(bootTime);
        audit = new Audit(logger, "test", OPPIJA, hostname, heartbeatDaemon, clock);
        when(op.name()).thenReturn("OP");
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
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("OP", r.get("operation").getAsString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void noOperation() {
        op = null;
        audit.log(user, op, target, new Changes.Builder().build());
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
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("null", Util.getJsonElementByPath(r, "changes.newValue").toString());
    }

    @Test
    public void nullValue() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", (JsonElement) null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("null", Util.getJsonElementByPath(r, "changes.newValue").toString());
    }

    @Test
    public void withChange() {
        audit.log(user, op, target, new Changes.Builder().updated("kenttä", "vanhaArvo", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
    }

    @Test
    public void withAddedString() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].oldValue").toString());
    }

    @Test
    public void withAddedObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("nestedKey", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added("kenttä", newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("{\"nestedKey\":\"uusiArvo\"}", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].oldValue").toString());
    }

    @Test
    public void withAddedJsonObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("kenttä", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added(newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].oldValue").toString());
    }

    @Test
    public void withAddedExisting() {
        audit.log(user, op, target, new Changes.Builder()
                .removed("kenttä", "vanhaArvo")
                .added("kenttä", "uusiArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
    }

    @Test
    public void withRemoved() {
        audit.log(user, op, target, new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].newValue").toString());
    }

    @Test
    public void withRemovedJsonObject() {
        JsonObject oldValue = new JsonObject();
        oldValue.add("kenttä", new JsonPrimitive("vanhaArvo"));
        audit.log(user, op, target, new Changes.Builder().removed(oldValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].newValue").toString());
    }

    @Test
    public void withRemovedExisting() {
        audit.log(user, op, target, new Changes.Builder()
                .added("kenttä", "uusiArvo")
                .removed("kenttä", "vanhaArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
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
    public void testJsonPatchDiff() {
        AuditTestDto changedDto = new AuditTestDto();
        changedDto.shortString = "wasp";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("shortString", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals(dto.shortString, Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals(changedDto.shortString, Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
    }

    @Test
    public void updateOfNestedJsonObjectGetsLoggedCorrectly() {
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        AuditTestDtoWithNumberString changedDtoWithNumberString = new AuditTestDtoWithNumberString(false);
        changedDtoWithNumberString.nestedDtoWithNumberString = new AuditTestDtoWithNumberString(false);

        audit.log(user, op, target, Changes.updatedDto(changedDtoWithNumberString, dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("nestedDtoWithNumberString", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].oldValue").toString());
        String escapedJsonString = gson.toJson(gson.toJsonTree(changedDtoWithNumberString.nestedDtoWithNumberString).toString());
        assertEquals(escapedJsonString, Util.getJsonElementByPath(r, "changes[0].newValue").toString());
    }

    @Test
    public void nestedDataInsideNestedDataPosesNoProblem() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.longString = "A slightly modified String to make us wonder.";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("longString", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals(dto.longString, Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals("A slightly modified String to make us wonder.", Util.getJsonElementByPath(r, "changes[0].newValue").getAsString());
    }

    @Test
    public void removalFromArrayGetsLoggedCorrectly() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.array = new String[] {};

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("array.0", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals("Similarly, a more moderate length string this time.", Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].newValue").toString());
    }

    @Test
    public void jsonPatchMoveIsLoggedAsRemoveAndAdd() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        String movingString = "Moving String";
        dto.shortString = movingString;
        dto.longString = null;
        changedDto.shortString = null;
        changedDto.longString = movingString;

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("shortString", Util.getJsonElementByPath(r, "changes[0].fieldName").getAsString());
        assertEquals(movingString, Util.getJsonElementByPath(r, "changes[0].oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[0].newValue").toString());
        assertEquals("longString", Util.getJsonElementByPath(r, "changes[1].fieldName").getAsString());
        assertEquals(movingString, Util.getJsonElementByPath(r, "changes[1].newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes[1].oldValue").toString());
    }

    @Test
    public void logsAlsoDeletionViaDtoApi() {
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        audit.log(user, op, target, Changes.deleteDto(dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new JsonPrimitive(gson.toJson(dtoWithNumberString)), Util.getJsonElementByPath(r, "changes[0].oldValue").getAsJsonPrimitive());
    }

    @Test
    public void getJsonElementByPathReturnsTheRightJsonElement() {
        JsonObject json = new JsonObject();
        JsonObject object = new JsonObject();
        object.add("abc", new JsonPrimitive(345));
        object.add("def", new JsonPrimitive("asd"));
        JsonArray array = new JsonArray();
        array.add("value3");
        array.add("value4");
        array.add(object);
        JsonArray array2 = new JsonArray();
        array2.add("value7");
        array2.add("value8");
        JsonObject object2 = new JsonObject();
        object2.add("key5", new JsonPrimitive("value6"));
        object2.add("key6", array2);
        json.add("abc", new JsonPrimitive(123));
        json.add("def", new JsonPrimitive("value2"));
        json.add("key3", array);
        json.add("key4", object2);

        assertEquals(json.get("abc"), Util.getJsonElementByPath(json, "abc"));
        assertEquals(json.get("def"), Util.getJsonElementByPath(json, "def"));
        assertEquals(array, Util.getJsonElementByPath(json, "key3"));
        assertEquals(array.get(1), Util.getJsonElementByPath(json, "key3[1]"));
        assertEquals(object.get("def"), Util.getJsonElementByPath(json, "key3[2].def"));
        assertEquals(object2, Util.getJsonElementByPath(json, "key4"));
        assertEquals(object2.get("key5"), Util.getJsonElementByPath(json, "key4.key5"));
        assertEquals(array2, Util.getJsonElementByPath(json, "key4.key6"));
        assertEquals(array2.get(0), Util.getJsonElementByPath(json, "key4.key6[0]"));
        assertEquals(array2.get(1), Util.getJsonElementByPath(json, "key4.key6[1]"));
        assertEquals(array2.get(1), Util.getJsonElementByPath(json, "key4.key6.1"));
        
        // Paths that don't point to any `JsonElement` should result in `null`.
        assertEquals("null", Util.getJsonElementByPath(json, "key7").toString());
        assertEquals("null", Util.getJsonElementByPath(json, "def[0]").toString());
        assertEquals("null", Util.getJsonElementByPath(json, "def[0].non-existent").toString());
        assertEquals("null", Util.getJsonElementByPath(json, "non-existent[0]").toString());
        assertEquals("null", Util.getJsonElementByPath(json, "non-existent[0][0]").toString());
        assertEquals("null", Util.getJsonElementByPath(json, "key4.key6.2").toString());
        assertEquals("null", Util.getJsonElementByPath(json, "key4.key7").toString());
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
        public String longString = "Not that long string that it would be truncated.";
        public String shortString = "bee";
        public int number = 99;
        public String[] array = new String[] { "Similarly, a more moderate length string this time." };
        public AuditTestDto nestedDto = null;

        public AuditTestDto(boolean withLongStrings) {
            if (withLongStrings) {
                longString = createLongString();
                array = new String[] { createLongString() };
            }
        }

        public AuditTestDto() {
            this(true);
        }
    }

    public static class AuditTestDtoWithNumberString {
        public String longString = "Not that long string that it would be truncated.";
        public String shortString = "bee";
        public String number = "99";
        public String[] array = new String[] { "Similarly, a more moderate length string this time." };
        public AuditTestDtoWithNumberString nestedDtoWithNumberString = null;

        public AuditTestDtoWithNumberString(boolean withLongStrings) {
            if (withLongStrings) {
                longString = createLongString();
                array = new String[] { createLongString() };
            }
        }

        public AuditTestDtoWithNumberString() {
            this(true);
        }
    }


}
