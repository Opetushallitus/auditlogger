package fi.vm.sade.auditlog;

import static fi.vm.sade.auditlog.Audit.MAX_FIELD_LENGTH;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.tananaev.jsonpatch.JsonPatch;
import com.tananaev.jsonpatch.JsonPatchFactory;
import com.tananaev.jsonpatch.JsonPath;
import com.tananaev.jsonpatch.operation.AbsOperation;
import com.tananaev.jsonpatch.operation.AddOperation;
import com.tananaev.jsonpatch.operation.ReplaceOperation;

import java.util.Iterator;
import java.util.Map;

public class Util {
    private static final JsonPatchFactory jsonPatchFactory = new JsonPatchFactory();

    static void traverseAndTruncate(JsonElement data) {
        if (data.isJsonObject()) {
            JsonObject object = data.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String fieldName = entry.getKey();
                JsonElement child = entry.getValue();
                if (isTextual(child)) {
                    object.addProperty(fieldName, truncate(child.getAsString()));
                } else {
                    traverseAndTruncate(child);
                }
            }
        } else if (data.isJsonArray()) {
            JsonArray array = data.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonElement child = array.get(i);
                if (isTextual(child)) {
                    array.set(i, new JsonPrimitive(truncate(child.getAsString())));
                } else {
                    traverseAndTruncate(child);
                }
            }
        }
    }

    private static String truncate(String data) {
        int maxLength = MAX_FIELD_LENGTH / 10; // Assume only a small number of fields can be extremely long
        if (data.length() <= maxLength) {
            return data;
        } else {
            return Integer.toString(data.hashCode());
        }
    }

    private static boolean isTextual(JsonElement e) {
        return e.isJsonPrimitive() && e.getAsJsonPrimitive().isString();
    }

    /**
     * Returns a JSON sub-element from the given JsonElement and the given path
     * Thankyou isapir https://stackoverflow.com/users/968244/isapir
     * at Stack Overflow https://stackoverflow.com/a/47744317
     *
     * @param json - a Gson JsonElement
     * @param path - a JSON path, e.g. a.b.c[2].d
     * @return - a sub-element of json according to the given path
     */
    public static JsonElement getJsonElement(JsonElement json, String path) {
        String[] parts = path.split("\\.|\\[|\\]");
        JsonElement result = json;

        for (String key : parts) {
            key = key.trim();
            if (key.isEmpty())
                continue;

            if (result == null) {
                result = JsonNull.INSTANCE;
                break;
            }

            if (result.isJsonObject()) {
                result = result.getAsJsonObject().get(key);
            } else if (result.isJsonArray()) {
                int ix = Integer.valueOf(key) - 1;
                result = result.getAsJsonArray().get(ix);
            } else break;
        }

        return result;
    }
}
