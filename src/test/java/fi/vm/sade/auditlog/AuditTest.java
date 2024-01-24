package fi.vm.sade.auditlog;

import static fi.vm.sade.auditlog.ApplicationType.OPPIJA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

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
    private ObjectMapper mapper = new ObjectMapper();
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
    public void sequence() throws JsonMappingException, JsonProcessingException{
        audit.log(user, op, target, new Changes.Builder().build());
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(2)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals(1, r.get("logSeq").asInt());
    }

    @Test
    public void timestamp() throws JsonMappingException, JsonProcessingException {
        Date now = new Date();
        when(clock.wallClockTime()).thenReturn(now);
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(now), r.get("timestamp").asText());
    }

    @Test
    public void bootTime() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").format(bootTime), r.get("bootTime").asText());
    }

    @Test
    public void operation() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("OP", r.get("operation").asText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void noOperation() {
        op = null;
        audit.log(user, op, target, new Changes.Builder().build());
    }

    @Test
    public void user() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        ObjectNode user = (ObjectNode) r.get("user");
        assertEquals("1.1.1.1.1.1.1", user.get("oid").asText());
        assertEquals("127.0.0.1", user.get("ip").asText());
        assertEquals("session-id", user.get("session").asText());
        assertEquals("user-agent", user.get("userAgent").asText());
    }

    @Test
    public void nullStringValue() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", (String) null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals(NullNode.getInstance(), r.at("/changes/0/newValue"));
    }

    @Test
    public void nullValue() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", (JsonNode) null).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals(NullNode.getInstance(), r.at("/changes/0/newValue"));
    }

    @Test
    public void withChange() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().updated("kenttä", "vanhaArvo", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("vanhaArvo", r.at("/changes/0/oldValue").asText());
        assertEquals("uusiArvo", r.at("/changes/0/newValue").asText());
    }

    @Test
    public void withAddedString() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().added("kenttä", "uusiArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("uusiArvo", r.at("/changes/0/newValue").asText());
        assertTrue(r.at("/changes/0/oldValue").isMissingNode());
    }

    @Test
    public void withAddedObject() throws JsonMappingException, JsonProcessingException {
        ObjectNode newValue = mapper.createObjectNode();
        newValue.put("nestedKey", "uusiArvo");
        audit.log(user, op, target, new Changes.Builder().added("kenttä", newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals(newValue.toString(), r.at("/changes/0/newValue").asText());
        assertTrue(r.at("/changes/0/oldValue").isMissingNode());
    }

    @Test
    public void withAddedJsonObject() throws JsonMappingException, JsonProcessingException {
        ObjectNode newValue = mapper.createObjectNode();
        newValue.put("kenttä", "uusiArvo");
        audit.log(user, op, target, new Changes.Builder().added(newValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("uusiArvo", r.at("/changes/0/newValue").asText());
        assertTrue(r.at("/changes/0/oldValue").isMissingNode());
    }

    @Test
    public void withAddedExisting() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder()
                .removed("kenttä", "vanhaArvo")
                .added("kenttä", "uusiArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("uusiArvo", r.at("/changes/0/newValue").asText());
        assertEquals("vanhaArvo", r.at("/changes/0/oldValue").asText());
    }

    @Test
    public void withRemoved() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("vanhaArvo", r.at("/changes/0/oldValue").asText());
        assertTrue(r.at("/changes/0/newValue").isMissingNode());
    }

    @Test
    public void withRemovedJsonObject() throws JsonMappingException, JsonProcessingException {
        ObjectNode oldValue = mapper.createObjectNode();
        oldValue.put("kenttä", "vanhaArvo");
        audit.log(user, op, target, new Changes.Builder().removed(oldValue).build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("vanhaArvo", r.at("/changes/0/oldValue").asText());
        assertTrue(r.at("/changes/0/newValue").isMissingNode());
    }

    @Test
    public void withRemovedExisting() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder()
                .added("kenttä", "uusiArvo")
                .removed("kenttä", "vanhaArvo")
                .build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("kenttä", r.at("/changes/0/fieldName").asText());
        assertEquals("uusiArvo", r.at("/changes/0/newValue").asText());
        assertEquals("vanhaArvo", r.at("/changes/0/oldValue").asText());
    }

    @Test
    public void withTarget() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op,
                new Target.Builder().setField("henkilö", "person-oid").setField("hakemus", "hakemus-oid").build(),
                new Changes.Builder().removed("kenttä", "vanhaArvo").build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        ObjectNode target = (ObjectNode) r.get("target");
        assertEquals("person-oid", target.get("henkilö").asText());
        assertEquals("hakemus-oid", target.get("hakemus").asText());
    }

    @Test
    public void logType() throws JsonMappingException, JsonProcessingException {
        audit.log(user, op, target, new Changes.Builder().build());
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("log", r.get("type").asText());
    }

    @Test
    public void aliveType() throws JsonMappingException, JsonProcessingException {
        audit.logHeartbeat();
        verify(logger, times(1)).log(msgCaptor.capture());
        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("alive", r.get("type").asText());
    }

    @Test
    public void testJsonPatchDiff() throws JsonMappingException, JsonProcessingException {
        AuditTestDto changedDto = new AuditTestDto();
        changedDto.shortString = "wasp";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("shortString", r.at("/changes/0/fieldName").asText());
        assertEquals(dto.shortString, r.at("/changes/0/oldValue").asText());
        assertEquals(changedDto.shortString, r.at("/changes/0/newValue").asText());
    }

    @Test
    public void updateOfNestedJsonObjectGetsLoggedCorrectly() throws JsonProcessingException{
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        AuditTestDtoWithNumberString changedDtoWithNumberString = new AuditTestDtoWithNumberString(false);
        changedDtoWithNumberString.nestedDtoWithNumberString = new AuditTestDtoWithNumberString(false);

        audit.log(user, op, target, Changes.updatedDto(changedDtoWithNumberString, dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("nestedDtoWithNumberString", r.at("/changes/0/fieldName").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/0/oldValue"));
        ObjectNode escapedJsonString = mapper.valueToTree(changedDtoWithNumberString.nestedDtoWithNumberString);
        assertEquals(escapedJsonString, r.at("/changes/0/newValue"));
    }

    @Test
    public void nestedDataInsideNestedDataPosesNoProblem() throws JsonMappingException, JsonProcessingException {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.longString = "A slightly modified String to make us wonder.";

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("longString", r.at("/changes/0/fieldName").asText());
        assertEquals(dto.longString, r.at("/changes/0/oldValue").asText());
        assertEquals("A slightly modified String to make us wonder.",
                     r.at("/changes/0/newValue").asText());
    }

    @Test
    public void removalFromArrayGetsLoggedCorrectly() throws JsonMappingException, JsonProcessingException {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        changedDto.array = new String[] {};

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("array.0", r.at("/changes/0/fieldName").asText());
        assertEquals("Similarly, a more moderate length string this time.",
                     r.at("/changes/0/oldValue").asText());
        assertTrue(r.at("/changes/0/newValue").isMissingNode());
    }

    @Test
    public void jsonPatchMoveIsLoggedAsRemoveAndAdd() throws JsonProcessingException {
        dto = new AuditTestDto(false);
        AuditTestDto changedDto = new AuditTestDto(false);
        String movingString = "Moving String";
        dto.shortString = movingString;
        dto.longString = null;
        changedDto.shortString = null;
        changedDto.longString = movingString;

        audit.log(user, op, target, Changes.updatedDto(changedDto, dto));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("longString", r.at("/changes/0/fieldName").asText());
        assertEquals(movingString, r.at("/changes/1/oldValue").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/1/newValue"));
        assertEquals("shortString", r.at("/changes/1/fieldName").asText());
        assertEquals(movingString, r.at("/changes/0/newValue").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/0/oldValue"));
    }

    @Test
    public void logsAlsoAdditionViaDtoApi() throws JsonProcessingException{
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        audit.log(user, op, target, Changes.addedDto(dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("longString", r.at("/changes/0/fieldName").asText());
        assertEquals("shortString", r.at("/changes/1/fieldName").asText());
        assertEquals("number", r.at("/changes/2/fieldName").asText());
        assertEquals("array", r.at("/changes/3/fieldName").asText());
        assertEquals("nestedDtoWithNumberString", r.at("/changes/4/fieldName").asText());
        assertEquals(dtoWithNumberString.longString, r.at("/changes/0/newValue").asText());
        assertEquals(dtoWithNumberString.shortString, r.at("/changes/1/newValue").asText());
        assertEquals(dtoWithNumberString.number, r.at("/changes/2/newValue").asText());
        assertEquals(mapper.valueToTree(dtoWithNumberString.array).toString(),
                     r.at("/changes/3/newValue").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/4/newValue"));
        assertTrue(r.at("/changes/0/oldValue").isMissingNode());
        assertTrue(r.at("/changes/1/oldValue").isMissingNode());
        assertTrue(r.at("/changes/2/oldValue").isMissingNode());
        assertTrue(r.at("/changes/3/oldValue").isMissingNode());
        assertTrue(r.at("/changes/4/oldValue").isMissingNode());
    }

    @Test
    public void logsAlsoDeletionViaDtoApi() throws JsonProcessingException{
        dtoWithNumberString = new AuditTestDtoWithNumberString(false);
        audit.log(user, op, target, Changes.deleteDto(dtoWithNumberString));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("longString", r.at("/changes/0/fieldName").asText());
        assertEquals("shortString", r.at("/changes/1/fieldName").asText());
        assertEquals("number", r.at("/changes/2/fieldName").asText());
        assertEquals("array", r.at("/changes/3/fieldName").asText());
        assertEquals("nestedDtoWithNumberString", r.at("/changes/4/fieldName").asText());
        assertEquals(dtoWithNumberString.longString, r.at("/changes/0/oldValue").asText());
        assertEquals(dtoWithNumberString.shortString, r.at("/changes/1/oldValue").asText());
        assertEquals(dtoWithNumberString.number, r.at("/changes/2/oldValue").asText());
        assertEquals(mapper.valueToTree(dtoWithNumberString.array).toString(),
                     r.at("/changes/3/oldValue").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/4/oldValue"));
        assertTrue(r.at("/changes/0/newValue").isMissingNode());
        assertTrue(r.at("/changes/1/newValue").isMissingNode());
        assertTrue(r.at("/changes/2/newValue").isMissingNode());
        assertTrue(r.at("/changes/3/newValue").isMissingNode());
        assertTrue(r.at("/changes/4/newValue").isMissingNode());
    }

    @Test
    public void updateOfMoreComplexObjectIsLoggedProperly() throws JsonMappingException, JsonProcessingException {
        ArrayNode array = mapper.createArrayNode();
        array.add("value3");
        array.add("value4");
        array.add(NullNode.getInstance());

        ArrayNode array2 = mapper.createArrayNode();
        array2.add(789);
        array2.add("value8");

        ObjectNode object2 = mapper.createObjectNode();
        object2.put("key5", "value6");
        object2.set("key6", array2);

        ObjectNode object = mapper.createObjectNode();
        object.put("key1", 123);
        object.put("key2", "value2");
        object.set("key3", array);
        object.set("key4", object2);

        ObjectNode changedObject = object.deepCopy();
        changedObject.put("key1", 1234);
        ((ArrayNode) changedObject.at("/key3")).remove(1);
        ((ArrayNode) changedObject.at("/key4/key6")).set(1, TextNode.valueOf("changedValue8"));
        ((ObjectNode) changedObject.at("/key4")).put("key5", "changedValue6");
        ((ObjectNode) changedObject.at("/key4")).put("key9", "addedValue9");

        audit.log(user, op, target, Changes.updatedDto(changedObject, object));
        verify(logger, times(1)).log(msgCaptor.capture());

        ObjectNode r = mapper.readValue(msgCaptor.getValue(), ObjectNode.class);
        assertEquals("key1", r.at("/changes/0/fieldName").asText());
        assertEquals(123, r.at("/changes/0/oldValue").asInt());
        assertEquals(1234, r.at("/changes/0/newValue").asInt());
        assertEquals("key3.2", r.at("/changes/1/fieldName").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/1/oldValue"));
        assertTrue(r.at("/changes/1/newValue").isMissingNode());
        assertEquals("key3.1", r.at("/changes/2/fieldName").asText());
        assertEquals("value4", r.at("/changes/2/oldValue").asText());
        assertEquals(NullNode.getInstance(), r.at("/changes/2/newValue"));
        assertEquals("key4.key9", r.at("/changes/3/fieldName").asText());
        assertEquals("addedValue9", r.at("/changes/3/newValue").asText());
        assertTrue(r.at("/changes/3/oldValue").isMissingNode());
        assertEquals("key4.key5", r.at("/changes/4/fieldName").asText());
        assertEquals("value6", r.at("/changes/4/oldValue").asText());
        assertEquals("changedValue6", r.at("/changes/4/newValue").asText());
        assertEquals("key4.key6.1", r.at("/changes/5/fieldName").asText());
        assertEquals("value8", r.at("/changes/5/oldValue").asText());
        assertEquals("changedValue8", r.at("/changes/5/newValue").asText());
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
