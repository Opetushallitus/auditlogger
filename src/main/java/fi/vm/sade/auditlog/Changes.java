package fi.vm.sade.auditlog;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.tananaev.jsonpatch.JsonPatch;
import com.tananaev.jsonpatch.JsonPatchFactory;
import com.tananaev.jsonpatch.JsonPath;
import com.tananaev.jsonpatch.operation.AbsOperation;
import com.tananaev.jsonpatch.operation.AddOperation;
import com.tananaev.jsonpatch.operation.ReplaceOperation;

import java.util.Iterator;

public final class Changes {
    private static final Gson gson = new Gson();
    private static final JsonPatchFactory jsonPatchFactory = new JsonPatchFactory();

    private JsonObject json = new JsonObject();

    private Changes() { }

    public static <T> Changes addedDto(T dto) {
        return create(dto, null);
    }

    private static <T> Changes create(T afterOperation, T beforeOperation) {
        Changes.Builder builder = new Changes.Builder();
        if (afterOperation == null && beforeOperation != null) {
            builder.removed("change", gson.toJsonTree(beforeOperation));
        } else if (afterOperation != null && beforeOperation == null) {
            builder.added("change", gson.toJsonTree(afterOperation));
        } else if (afterOperation != null) {
            JsonElement afterJson = gson.toJsonTree(afterOperation);
            JsonElement beforeJson = gson.toJsonTree(beforeOperation);
            builder.jsonDiffToChanges(beforeJson, afterJson);
        }
        return builder.build();
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
            Util.traverseAndTruncate(r.json);
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

        Builder jsonDiffToChanges(JsonElement beforeJson, JsonElement afterJson) {
            Util.traverseAndTruncate(afterJson);
            Util.traverseAndTruncate(beforeJson);
            JsonPatch patchArray = jsonPatchFactory.create(beforeJson, afterJson);
            for (Iterator<AbsOperation> it = patchArray.iterator(); it.hasNext(); ) {
                AbsOperation absOperation = it.next();
                String operation = absOperation.getOperationName();
                JsonPath path = absOperation.path;

                String prettyPath = path.toString().substring(1).replaceAll("/", ".");
                switch (operation) {
                    case "add": {
                        added(prettyPath, ((AddOperation) absOperation).data.getAsString());
                        break;
                    }
                    case "remove": {
                        JsonElement oldValue = Util.getJsonElement(beforeJson, prettyPath);
                        removed(prettyPath, oldValue.getAsString());
                        break;
                    }
                    case "replace": {
                        JsonElement oldValue = Util.getJsonElement(beforeJson, prettyPath);
                        JsonElement newValue = ((ReplaceOperation) absOperation).data;
                        updated(prettyPath, oldValue.getAsString(), newValue.getAsString());
                        break;
                    }
                    default: throw new IllegalArgumentException("Unknown operation " + operation);
                }
            }
            return this;
        }

        private boolean hasChange(JsonElement oldValue, JsonElement newValue) {
            return null == oldValue ? null != newValue : !oldValue.equals(newValue);
        }
    }
}
