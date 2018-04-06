package fi.vm.sade.auditlog;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

import java.io.IOException;

public class AuditLogTest {
    private final static Gson gson = new Gson();

    private String longString = createLongString();

    private String jsonString =
                "{" +
                    " \"longString\": \"" + longString + "\"," +
                    " \"shortString\": \"bee\"," +
                    " \"number\": 99," +
                    " \"array\": [ \"" + longString + "\" ] " +
                "}";

    @Test
    public void truncatesLongField() throws IOException {
        JsonElement json = gson.fromJson(jsonString, JsonElement.class);
        assert(json.toString().length() > Audit.MAX_FIELD_LENGTH);

        Util.traverseAndTruncate(json);

        String truncatedString = json.getAsJsonObject().get("longString").getAsString();
        assertTrue(truncatedString.length() < longString.length());
        assertTrue(truncatedString.length() < Audit.MAX_FIELD_LENGTH);
        assertTrue(json.toString().length() < Audit.MAX_FIELD_LENGTH);
    }

    @Test
    public void truncatesLongArrayElement() throws IOException {

        JsonElement json = gson.fromJson(jsonString, JsonElement.class);
        assertTrue(json.toString().length() > Audit.MAX_FIELD_LENGTH);

        Util.traverseAndTruncate(json);

        String truncatedString = json.getAsJsonObject().get("array").getAsJsonArray().get(0).getAsString();
        assertTrue(truncatedString.length() < longString.length());
        assertTrue(truncatedString.length() < Audit.MAX_FIELD_LENGTH);
        assertTrue(json.toString().length() < Audit.MAX_FIELD_LENGTH);
    }

    @Test
    public void truncatedStringsMatchForIdenticalInputs() throws IOException {
        JsonElement json = gson.fromJson(jsonString, JsonElement.class);
        Util.traverseAndTruncate(json);

        String truncatedString1 = json.getAsJsonObject().get("longString").getAsString();
        String truncatedString2 = json.getAsJsonObject().get("array").getAsJsonArray().get(0).getAsString();
        assertEquals(truncatedString1, truncatedString2);
    }

    @Test
    public void doesNotTruncateShortField() throws IOException {
        JsonElement json = gson.fromJson(jsonString, JsonElement.class);
        Util.traverseAndTruncate(json);

        String shortString = json.getAsJsonObject().get("shortString").getAsString();
        assertEquals("bee", shortString);
    }

    @Test
    public void doesNotTruncateNumber() throws IOException {
        JsonElement json = gson.fromJson(jsonString, JsonElement.class);
        Util.traverseAndTruncate(json);

        int number = json.getAsJsonObject().get("number").getAsInt();
        assertEquals(99, number);
    }

    @Test
    public void testJsonPatchDiff() throws IOException {
        String changedJsonString = "{" +
                " \"longString\": \"" + longString + "\"," +
                " \"shortString\": \"wasp\"," +
                " \"number\": 99," +
                " \"array\": [ \"" + longString + "\" ] " +
                "}";

        JsonElement json = gson.fromJson(jsonString, JsonElement.class);
        JsonElement jsonChanged = gson.fromJson(changedJsonString, JsonElement.class);
        Changes.Builder builder = new Changes.Builder();
        Util.jsonDiffToChanges(builder, json, jsonChanged);
        Changes build = builder.build();

        JsonObject jsonObject = build.asJson();
        assertEquals(jsonObject.get("shortString").getAsJsonObject().get("oldValue").getAsString(), "bee");
        assertEquals(jsonObject.get("shortString").getAsJsonObject().get("newValue").getAsString(), "wasp");
    }


    private String createLongString() {
        int length = 33000;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }
}
