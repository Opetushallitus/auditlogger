package fi.vm.sade.auditlog;

import static fi.vm.sade.auditlog.ApplicationType.OPPIJA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.*;
import com.tananaev.jsonpatch.JsonPath;

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
        assertEquals("null", path("/changes/0/newValue").navigate(r).toString());
    }

    @Test
    public void nullValue() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", (JsonElement) null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("null", path("/changes/0/newValue").navigate(r).toString());
    }

    @Test
    public void withChange() {
        audit.log(user, op, target, new Changes.Builder().updated("kenttä", "vanhaArvo", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("vanhaArvo", path("/changes/0/oldValue").navigate(r).getAsString());
        assertEquals("uusiArvo", path("/changes/0/newValue").navigate(r).getAsString());
    }

    @Test
    public void withAddedString() {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("uusiArvo", path("/changes/0/newValue").navigate(r).getAsString());
        assertNull(path("/changes/0/oldValue").navigate(r));
    }

    @Test
    public void withAddedObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("nestedKey", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added("kenttä", newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("{\"nestedKey\":\"uusiArvo\"}", path("/changes/0/newValue").navigate(r).getAsString());
        assertNull(path("/changes/0/oldValue").navigate(r));
    }

    @Test
    public void withAddedJsonObject() {
        JsonObject newValue = new JsonObject();
        newValue.add("kenttä", new JsonPrimitive("uusiArvo"));
        audit.log(user, op, target, new Changes.Builder().added(newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("uusiArvo", path("/changes/0/newValue").navigate(r).getAsString());
        assertNull(path("/changes/0/oldValue").navigate(r));
    }

    @Test
    public void withAddedExisting() {
        audit.log(user, op, target, new Changes.Builder()
                .removed("kenttä", "vanhaArvo")
                .added("kenttä", "uusiArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("uusiArvo", path("/changes/0/newValue").navigate(r).getAsString());
        assertEquals("vanhaArvo", path("/changes/0/oldValue").navigate(r).getAsString());
    }

    @Test
    public void withRemoved() {
        audit.log(user, op, target, new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("vanhaArvo", path("/changes/0/oldValue").navigate(r).getAsString());
        assertNull(path("/changes/0/newValue").navigate(r));
    }

    @Test
    public void withRemovedJsonObject() {
        JsonObject oldValue = new JsonObject();
        oldValue.add("kenttä", new JsonPrimitive("vanhaArvo"));
        audit.log(user, op, target, new Changes.Builder().removed(oldValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("vanhaArvo", path("/changes/0/oldValue").navigate(r).getAsString());
        assertNull(path("/changes/0/newValue").navigate(r));
    }

    @Test
    public void withRemovedExisting() {
        audit.log(user, op, target, new Changes.Builder()
                .added("kenttä", "uusiArvo")
                .removed("kenttä", "vanhaArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("kenttä", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("uusiArvo", path("/changes/0/newValue").navigate(r).getAsString());
        assertEquals("vanhaArvo", path("/changes/0/oldValue").navigate(r).getAsString());
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
        assertEquals("shortString", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals(dto.shortString, path("/changes/0/oldValue").navigate(r).getAsString());
        assertEquals(changedDto.shortString, path("/changes/0/newValue").navigate(r).getAsString());
    }

    @Test
    public void updateOfNestedJsonObjectGetsLoggedCorrectly() {
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        AuditTestDtoWithNumberString changedDtoWithNumberString = new AuditTestDtoWithNumberString(false);
        changedDtoWithNumberString.nestedDtoWithNumberString = new AuditTestDtoWithNumberString(false);

        audit.log(user, op, target, Changes.updatedDto(changedDtoWithNumberString, dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("nestedDtoWithNumberString", path("/changes/0/fieldName").navigate(r).getAsString());
        assertNull(path("/changes/0/oldValue").navigate(r));
        String escapedJsonString = gson.toJson(gson.toJsonTree(changedDtoWithNumberString.nestedDtoWithNumberString).toString());
        assertEquals(escapedJsonString, path("/changes/0/newValue").navigate(r).toString());
    }

    @Test
    public void nestedDataInsideNestedDataPosesNoProblem() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.longString = "A slightly modified String to make us wonder.";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("longString", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals(dto.longString, path("/changes/0/oldValue").navigate(r).getAsString());
        assertEquals("A slightly modified String to make us wonder.",
                     path("/changes/0/newValue").navigate(r).getAsString());
    }

    @Test
    public void removalFromArrayGetsLoggedCorrectly() {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.array = new String[] {};

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals("array.0", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals("Similarly, a more moderate length string this time.",
                     path("/changes/0/oldValue").navigate(r).getAsString());
        assertNull(path("/changes/0/newValue").navigate(r));
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
        System.out.println(r);
        System.out.println(path("/changes/0/fieldName").navigate(r));
        assertEquals("shortString", path("/changes/0/fieldName").navigate(r).getAsString());
        assertEquals(movingString, path("/changes/0/oldValue").navigate(r).getAsString());
        assertNull(path("/changes/0/newValue").navigate(r));
        assertEquals("longString", path("/changes/1/fieldName").navigate(r).getAsString());
        assertEquals(movingString, path("/changes/1/newValue").navigate(r).getAsString());
        assertNull(path("/changes/1/oldValue").navigate(r));
    }

    @Test
    public void logsAlsoDeletionViaDtoApi() {
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        audit.log(user, op, target, Changes.deleteDto(dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        JsonObject r = gson.fromJson(msgCaptor.getValue(), JsonObject.class);
        assertEquals(new JsonPrimitive(gson.toJson(dtoWithNumberString)),
                     path("/changes/0/oldValue").navigate(r).getAsJsonPrimitive());
    }

    private JsonPath path(String pathString) {
        return new JsonPath(pathString);
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
