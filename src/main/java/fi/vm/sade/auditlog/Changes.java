package fi.vm.sade.auditlog;

import static fi.vm.sade.auditlog.Audit.MAX_FIELD_LENGTH;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.tananaev.jsonpatch.JsonPatch;
import com.tananaev.jsonpatch.JsonPatchFactory;
import com.tananaev.jsonpatch.JsonPath;
import com.tananaev.jsonpatch.operation.AbsOperation;
import com.tananaev.jsonpatch.operation.AddOperation;
import com.tananaev.jsonpatch.operation.MoveOperation;
import com.tananaev.jsonpatch.operation.ReplaceOperation;

import java.util.Iterator;
import java.util.Map;

public final class Changes {
    public static final Changes EMPTY = new Changes.Builder().build();
    private static final Gson gson = new Gson();
    private static final JsonPatchFactory jsonPatchFactory = new JsonPatchFactory();

    private JsonObject json = new JsonObject();

    private Changes() { }

    public static <T> Changes addedDto(T dto) {
        return new Changes.Builder().added("change", gson.toJsonTree(dto)).build();
    }

    public static <T> Changes updatedDto(T dtoAfter, T dtoBefore) {
        JsonElement afterJson = gson.toJsonTree(dtoAfter);
        JsonElement beforeJson = gson.toJsonTree(dtoBefore);
        return new Changes.Builder().jsonDiffToChanges(beforeJson, afterJson).build();
    }

    public static <T> Changes deleteDto(T dto) {
        return new Changes.Builder().removed("change", gson.toJsonTree(dto)).build();
    }

    public JsonObject asJson() {
        return this.json;
    }

    public static class Builder {
        private Changes changes;

        public Builder() {
            this.changes = new Changes();
        }

        public Changes build() {
            Changes r = this.changes;
            this.changes = new Changes();
            traverseAndTruncate(r.json);
            return r;
        }

        public Builder added(String field, String newValue) {
           return this.added(field,
                   newValue == null ? null : new JsonPrimitive(newValue));
        }

        public Builder added(String field, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            JsonObject o = new JsonObject();
            o.add("newValue", newValue);
            changes.json.add(field, o);
            return this;
        }

        public Builder removed(String field, String oldValue) {
            return this.removed(field,
                    oldValue == null ? null : new JsonPrimitive(oldValue));
        }

        public Builder removed(String field, JsonElement oldValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            JsonObject o = new JsonObject();
            o.add("oldValue", oldValue);
            changes.json.add(field, o);
            return this;
        }

        public Builder updated(String field, String oldValue, String newValue) {
            return this.updated(
                    field,
                    oldValue == null ? null : new JsonPrimitive(oldValue),
                    newValue == null ? null : new JsonPrimitive(newValue));
        }

        public Builder updated(String field, JsonElement oldValue, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            if (hasChange(oldValue, newValue)) {
                JsonObject o = new JsonObject();
                o.add("oldValue", oldValue);
                o.add("newValue", newValue);
                changes.json.add(field, o);
            }
            return this;
        }

        private Builder jsonDiffToChanges(JsonElement beforeJson, JsonElement afterJson) {
            traverseAndTruncate(afterJson);
            traverseAndTruncate(beforeJson);
            JsonPatch patchArray = jsonPatchFactory.create(beforeJson, afterJson);
            for (Iterator<AbsOperation> it = patchArray.iterator(); it.hasNext(); ) {
                AbsOperation absOperation = it.next();
                String operation = absOperation.getOperationName();
                JsonPath path = absOperation.path;

                String prettyPath = prettify(path);
                switch (operation) {
                    case "add": {
                        added(prettyPath, toJsonString(((AddOperation) absOperation).data));
                        break;
                    }
                    case "remove": {
                        JsonElement oldValue = Util.getJsonElementByPath(beforeJson, prettyPath);
                        removed(prettyPath, toJsonString(oldValue));
                        break;
                    }
                    case "replace": {
                        JsonElement oldValue = Util.getJsonElementByPath(beforeJson, prettyPath);
                        JsonElement newValue = ((ReplaceOperation) absOperation).data;
                        updated(prettyPath, toJsonString(oldValue), toJsonString(newValue));
                        break;
                    }
                    case "move": {
                        String prettyFromPath = prettify(((MoveOperation) absOperation).from);
                        JsonElement oldValue = Util.getJsonElementByPath(beforeJson, prettyFromPath);
                        removed(prettyFromPath, toJsonString(oldValue));

                        JsonElement newValue = Util.getJsonElementByPath(afterJson, prettyPath);
                        added(prettyPath, newValue);
                        break;
                    }
                    default: throw new IllegalArgumentException(String.format("Unknown operation %s in %s . before: %s . after: %s"
                        , operation, absOperation.path, beforeJson, afterJson));
                }
            }
            return this;
        }

        private boolean hasChange(JsonElement oldValue, JsonElement newValue) {
            return null == oldValue ? null != newValue : !oldValue.equals(newValue);
        }

        private void traverseAndTruncate(JsonElement data) {
            if (data.isJsonObject()) {
                JsonObject object = data.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    String fieldName = entry.getKey();
                    JsonElement child = entry.getValue();
                    if (isTextual(child)) {
                        object.addProperty(fieldName, truncate(toJsonString(child)));
                    } else {
                        traverseAndTruncate(child);
                    }
                }
            } else if (data.isJsonArray()) {
                JsonArray array = data.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement child = array.get(i);
                    if (isTextual(child)) {
                        array.set(i, new JsonPrimitive(truncate(toJsonString(child))));
                    } else {
                        traverseAndTruncate(child);
                    }
                }
            }
        }

        private String truncate(String data) {
            int maxLength = MAX_FIELD_LENGTH / 10; // Assume only a small number of fields can be extremely long
            if (data.length() <= maxLength) {
                return data;
            } else {
                return Integer.toString(data.hashCode());
            }
        }

        private boolean isTextual(JsonElement e) {
            return e.isJsonPrimitive() && e.getAsJsonPrimitive().isString();
        }
    }

    private static String prettify(JsonPath path) {
        return path.toString().substring(1).replaceAll("/", ".");
    }

    private static String toJsonString(JsonElement element) {
        if (element.isJsonPrimitive() || element.isJsonNull()) {
            return element.getAsString();
        } else {
            return gson.toJson(element);
        }
    }
}
