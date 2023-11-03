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
    @Mock
    private Logger logger;
    @Mock
    private HeartbeatDaemon heartbeatDaemon;
    @Mock
    private Clock clock;
    @Mock
    private Operation op;
    @Captor
    private ArgumentCaptor<String> msgCaptor;

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

    public AuditTest() throws UnknownHostException, GSSException {
    }

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
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes.oldValue").getAsString());
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes.newValue").getAsString());
    }

    @Test
    public void withAddedString() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes.newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes.oldValue").toString());
    }

    @Test
    public void withAddedObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("nestedKey", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added("kenttä", newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("{\"nestedKey\":\"uusiArvo\"}", Util.getJsonElementByPath(r, "changes.kenttä.newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes.kenttä.oldValue").toString());
    }

    @Test
    public void withAddedJsonObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("kenttä", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added(newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes.kenttä.newValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes.kenttä.oldValue").toString());
    }

    @Test
    public void withAddedExisting() {
        audit.log(user, op, target, new Changes.Builder()
                .removed("kenttä", "vanhaArvo")
                .added("kenttä", "uusiArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes.kenttä.newValue").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes.kenttä.oldValue").getAsString());
    }

    @Test
    public void withRemoved() {
        audit.log(user, op, target, new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes.kenttä.oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes.kenttä.newValue").toString());
    }

    @Test
    public void withRemovedJsonObject() {
        JsonObject oldValue = new JsonObject();
        oldValue.add("kenttä", new JsonPrimitive("vanhaArvo"));
        audit.log(user, op, target, new Changes.Builder().removed(oldValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes.kenttä.oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes.kenttä.newValue").toString());
    }

    @Test
    public void withRemovedExisting() {
        audit.log(user, op, target, new Changes.Builder()
                .added("kenttä", "uusiArvo")
                .removed("kenttä", "vanhaArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        JsonArray changes = r.getAsJsonArray("changes");
        assertEquals("uusiArvo", Util.getJsonElementByPath(r, "changes.kenttä.newValue").getAsString());
        assertEquals("vanhaArvo", Util.getJsonElementByPath(r, "changes.kenttä.oldValue").getAsString());
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
        assert (gson.toJsonTree(dto).toString().length() > Audit.MAX_FIELD_LENGTH);

        audit.log(user, op, target, Changes.addedDto(dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("log", r.get("type").getAsString());
        String truncatedString = Util.getJsonElementByPath(r, "changes.newValue").getAsString();
        assertTrue(truncatedString.length() < dto.longString.length());
        assertTrue(truncatedString.length() < Audit.MAX_FIELD_LENGTH);
        assertTrue(r.toString().length() < Audit.MAX_FIELD_LENGTH);
    }

    @Test
    public void truncatesLongArrayElement() {
        assert (gson.toJsonTree(dto).toString().length() > Audit.MAX_FIELD_LENGTH);

        audit.log(user, op, target, Changes.addedDto(dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("log", r.get("type").getAsString());
        String truncatedString = Util.getJsonElementByPath(r, "changes.newValue").getAsString();
        assertTrue(truncatedString.length() < dto.longString.length());
        assertTrue(truncatedString.length() < Audit.MAX_FIELD_LENGTH);
        assertTrue(r.toString().length() < Audit.MAX_FIELD_LENGTH);
    }

    @Test
    public void truncatedStringsMatchForIdenticalInputs() {
        audit.log(user, op, target, Changes.addedDto(dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        String truncatedString1 = Util.getJsonElementByPath(r, "changes.longString.newValue").getAsString();
        String truncatedString2 = Util.getJsonElementByPath(r, "changes.array.newValue").getAsString();
        assertEquals(truncatedString1, truncatedString2);
    }

    @Test
    public void truncateChangesWithLongString() {
        audit.log(user, op, target, Changes.addedDto(dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        String loggedInt = Util.getJsonElementByPath(r, "changes.newValue").getAsString();
        assertEquals("1258294166", loggedInt);
    }

    @Test
    public void testJsonPatchDiff() {
        AuditTestDto changedDto = new AuditTestDto();
        changedDto.shortString = "wasp";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(dto.shortString, Util.getJsonElementByPath(r, "changes.shortString.oldValue").getAsString());
        assertEquals(changedDto.shortString, Util.getJsonElementByPath(r, "changes.shortString.newValue").getAsString());
    }

    @Test
    public void updateOfNestedJsonObjectGetsLoggedCorrectly() {
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        AuditTestDtoWithNumberString changedDtoWithNumberString = new AuditTestDtoWithNumberString(false);
        changedDtoWithNumberString.nestedDtoWithNumberString = new AuditTestDtoWithNumberString(false);

        audit.log(user, op, target, Changes.updatedDto(changedDtoWithNumberString, dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("null", Util.getJsonElementByPath(r, "changes.nestedDtoWithNumberString.oldValue").toString());
        String escapedJsonString = gson.toJson(gson.toJsonTree(changedDtoWithNumberString.nestedDtoWithNumberString).toString());
        assertEquals(escapedJsonString, Util.getJsonElementByPath(r, "changes.nestedDtoWithNumberString.newValue").toString());
    }

    @Test
    public void nestedDataInsideNestedDataPosesNoProblem() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.longString = "A slightly modified String to make us wonder.";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(dto.longString, Util.getJsonElementByPath(r, "changes.longString.oldValue").getAsString());
        assertEquals("A slightly modified String to make us wonder.", Util.getJsonElementByPath(r, "changes.longString.newValue").getAsString());
    }

    @Test
    public void removalFromArrayGetsLoggedCorrectly() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.array = new String[]{};

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("Similarly, a more moderate length string this time.", Util.getJsonElementByPath(r, "changes.array.0.oldValue").getAsString());
        assertEquals("null", Util.getJsonElementByPath(r, "changes.array.0.newValue").toString());
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
        assertEquals(movingString, Util.getJsonElementByPath(r, "changes.shortString.oldValue").getAsString());
        assertEquals(movingString, Util.getJsonElementByPath(r, "changes.longString.newValue").getAsString());
    }

    @Test
    public void logsAlsoDeletionViaDtoApi() {
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        audit.log(user, op, target, Changes.deleteDto(dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new JsonPrimitive(gson.toJson(dtoWithNumberString)), Util.getJsonElementByPath(r, "changes.oldValue").getAsJsonPrimitive());
    }

    @Test
    public void arrayAddAndRemoveAreCombinedIntoReplace() {
        JsonObject before = new JsonObject();
        JsonObject after = new JsonObject();

        JsonObject objWithNilValue = new JsonObject();
        objWithNilValue.addProperty("x", (String) null);
        JsonObject objWithPrimitiveValue = new JsonObject();
        objWithPrimitiveValue.addProperty("x", 1);

        JsonArray xs = new JsonArray();
        xs.add(objWithNilValue);
        xs.add(objWithNilValue);

        JsonArray ys = new JsonArray();
        ys.add(objWithNilValue);
        ys.add(objWithPrimitiveValue);

        before.add("a", xs);
        after.add("a", ys);
        JsonArray changes = Changes.updatedDto(after, before).asJsonArray();
        assertEquals(1, changes.size());
        JsonObject replaceOp = changes.get(0).getAsJsonObject();
        assertEquals("a.1", replaceOp.get("fieldName").getAsString());
        assertEquals("{\"x\":\"1\"}", replaceOp.get("newValue").getAsString());
        // An object containing a key with null value gets converted to just null
        // when retrieved with fi.vm.sade.auditlog.Util.getJsonElementByPath
        assertEquals("null", replaceOp.get("oldValue").getAsString());
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
        public String[] array = new String[]{"Similarly, a more moderate length string this time."};
        public AuditTestDto nestedDto = null;

        public AuditTestDto(boolean withLongStringsThatNeedToBeTruncated) {
            if (withLongStringsThatNeedToBeTruncated) {
                longString = createLongString();
                array = new String[]{createLongString()};
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
        public String[] array = new String[]{"Similarly, a more moderate length string this time."};
        public AuditTestDtoWithNumberString nestedDtoWithNumberString = null;

        public AuditTestDtoWithNumberString(boolean withLongStringsThatNeedToBeTruncated) {
            if (withLongStringsThatNeedToBeTruncated) {
                longString = createLongString();
                array = new String[]{createLongString()};
            }
        }

        public AuditTestDtoWithNumberString() {
            this(true);
        }
    }


}
