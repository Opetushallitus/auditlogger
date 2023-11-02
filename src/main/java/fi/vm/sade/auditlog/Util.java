package fi.vm.sade.auditlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

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
        return Util.getJsonElementByPath(json, path, false);
    }

    /**
     * Returns a JSON sub-element from the given JsonElement and the given path
     * Thankyou isapir https://stackoverflow.com/users/968244/isapir
     * at Stack Overflow https://stackoverflow.com/a/47744317
     *
     * @param json - a Gson JsonElement
     * @param path - a JSON path, e.g. a.b.c[2].d
     * @param returnJsonLeaf - set to true if you want to return a JsonArray matching the path;
     *                       set to false if you want to return null instead in case of a match
     * @return - a sub-element of json according to the given path
     */
    public static JsonElement getJsonElementByPath(JsonElement json, String path, boolean returnJsonLeaf) {
        String[] parts = path.split("\\.|\\[|\\]");
        JsonElement result = json;
        String lastKey = parts[parts.length-1];
        for (String key : parts) {
            key = key.trim();
            if (key.isEmpty())
                continue;

            if (result == null) {
                result = JsonNull.INSTANCE;
                return result;
            }

            if (result.isJsonObject()) {
                result = result.getAsJsonObject().get(key);
            } else if (result.isJsonArray()) {
                try {
                    int ix = Integer.valueOf(key);
                    result = result.getAsJsonArray().get(ix);
                } catch (NumberFormatException e) {
                    for (int i = 0; i < result.getAsJsonArray().size(); i++) {
                        JsonObject j = result.getAsJsonArray().get(i).getAsJsonObject();
                        if (j.has(key) && j.has(lastKey)) {
                            result = j.get(lastKey);
                            return result;
                        }
                    }
                }
            } else {
                return result;
            }

        }
        if (result == null) {
            return JsonNull.INSTANCE;
        }
        if (result.isJsonPrimitive()) {
            return result;
        }
        if (returnJsonLeaf && result.isJsonArray()) {
            return result;
        }
        result = JsonNull.INSTANCE;
        return result;
    }

}
