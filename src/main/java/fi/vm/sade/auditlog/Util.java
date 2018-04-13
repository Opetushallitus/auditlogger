package fi.vm.sade.auditlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class Util {
    /**
     * Returns a JSON sub-element from the given JsonElement and the given path
     * Thankyou isapir https://stackoverflow.com/users/968244/isapir
     * at Stack Overflow https://stackoverflow.com/a/47744317
     *
     * @param json - a Gson JsonElement
     * @param path - a JSON path, e.g. a.b.c[2].d
     * @return - a sub-element of json according to the given path
     */
    public static JsonElement getJsonElementByPath(JsonElement json, String path) {
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
                int ix = Integer.valueOf(key);
                result = result.getAsJsonArray().get(ix);
            } else break;
        }

        return result;
    }
}
