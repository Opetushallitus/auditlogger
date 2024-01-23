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
     * @return - a sub-element of json according to the given `path` or
     *           `JsonNull.INSTANCE` if no element can be found.
     */
    public static JsonElement getJsonElementByPath(JsonElement json, String path) {
        for (String key : path.split("\\.|\\[|\\]\\.|\\]")) {
            if (json == null) {
                return JsonNull.INSTANCE;
            }

            if (json.isJsonObject()) {
                json = json.getAsJsonObject().get(key);
            } else if (json.isJsonArray()) {
                try {
                    int ix = Integer.valueOf(key);
                    json = json.getAsJsonArray().get(ix);
                } catch (NumberFormatException|IndexOutOfBoundsException e) {
                    return JsonNull.INSTANCE;
                }
            } else {
                // There is no `key` in `JsonPrimitive` or `JsonNull` so in this
                // case the given `path` doesn't point to any `JsonElement`.
                // Thus, we return `JsonNull.INSTANCE`.
                return JsonNull.INSTANCE;
            }
        }
        return (json == null) ? JsonNull.INSTANCE : json;
    }
}
