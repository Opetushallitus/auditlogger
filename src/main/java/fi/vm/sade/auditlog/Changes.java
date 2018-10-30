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

    //private JsonObject json = new JsonObject();
    private JsonArray jsonArray = new JsonArray();

    private Changes() { }

    public static <T> Changes addedDto(T dto) {
        //TODO: halutaanko fieldName näihin?!
        return new Changes.Builder().added(gson.toJsonTree(dto)).build();
        //return new Changes.Builder().added("change", gson.toJsonTree(dto)).build();
    }

    public static <T> Changes updatedDto(T dtoAfter, T dtoBefore) {
        JsonElement afterJson = gson.toJsonTree(dtoAfter);
        JsonElement beforeJson = gson.toJsonTree(dtoBefore);
        return new Changes.Builder().jsonDiffToChanges(beforeJson, afterJson).build();
    }

    public static <T> Changes deleteDto(T dto) {
        //TODO: halutaanko fieldName näihin?!
        return new Changes.Builder().removed(gson.toJsonTree(dto)).build();
    }

    //public JsonObject asJson() {
    //    return this.json;
    //}

    public JsonArray asJsonArray() {
        return this.jsonArray;
    }
    public static class Builder {
        private Changes changes;

        public Builder() {
            this.changes = new Changes();
        }

        public Changes build() {
            Changes r = this.changes;
            this.changes = new Changes();
            traverseAndTruncate(r.jsonArray);
            //traverseAndTruncate(r.json);
            return r;
        }

        public Builder added(String field, String newValue) {
            return this.added(field,
                    newValue == null ? null : new JsonPrimitive(newValue));
        }

/*
        public Builder added(JsonElement newValue) {
            if (!newValue.isJsonNull()) {
                changes.json.add("newValue", new JsonPrimitive(toJsonString(newValue)));
            }
            return this;
        }
*/
        public Builder added(JsonElement newValue) {
            if (!newValue.isJsonNull()) {
                JsonObject j = new JsonObject();
                //j.add("fieldName", new JsonPrimitive("NA"));
                //j.add("oldValue", new JsonPrimitive("NA"));
                j.add("newValue", new JsonPrimitive(toJsonString(newValue)));
                changes.jsonArray.add(j);
            }
            return this;
        }
/*        public Builder added(String field, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            JsonObject o = changes.json.getAsJsonObject(field);
            if (o == null) {
                o = new JsonObject();
                changes.json.add(field, o);
            }
            o.add("newValue", newValue);
            return this;
        }
*/

        public Builder added(String field, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }

            JsonObject o = new JsonObject();
            for (int i = 0; i < changes.jsonArray.size(); i++) {
                JsonObject j = changes.jsonArray.get(i).getAsJsonObject();
                if (j.get("fieldName").equals(new JsonPrimitive(field))) {
                    o = j;
                    break;
                }
            }

            if (o.entrySet().size() == 0) {
                o = new JsonObject();
                o.add("fieldName", new JsonPrimitive(field));
                changes.jsonArray.add(o);
            }
            if (newValue == null || newValue.isJsonPrimitive()) {
                o.add("newValue", newValue);
            } else {
                o.add("newValue", new JsonPrimitive(toJsonString(newValue)));
            }
            return this;
        }

        public Builder added(JsonObject newValues) {
            if (newValues != null) {
                for (Map.Entry<String, JsonElement> entry : newValues.entrySet()) {
                    added(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder removed(String field, String oldValue) {
            return this.removed(field,
                    oldValue == null ? null : new JsonPrimitive(oldValue));
        }

/*
        public Builder removed(JsonElement oldValue) {
            if (!oldValue.isJsonNull()) {
                changes.json.add("oldValue", new JsonPrimitive(toJsonString(oldValue)));
            }
            return this;
        }
*/

        public Builder removed(JsonElement oldValue) {
            if (!oldValue.isJsonNull()) {
                JsonObject j = new JsonObject();
                //j.add("fieldName", new JsonPrimitive("NA"));
                j.add("oldValue", new JsonPrimitive(toJsonString(oldValue)));
                //j.add("newValue", new JsonPrimitive("NA"));
                changes.jsonArray.add(j);
            }
            return this;
        }

/*
        public Builder removed(String field, JsonElement oldValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            JsonObject o = changes.json.getAsJsonObject(field);
            if (o == null) {
                o = new JsonObject();
                changes.json.add(field, o);
            }
            o.add("oldValue", oldValue);
            return this;
        }
*/

        public Builder removed(String field, JsonElement oldValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }

            JsonObject o = new JsonObject();
            for(int i = 0; i < changes.jsonArray.size(); i++) {
                JsonObject j = changes.jsonArray.get(i).getAsJsonObject();
                if(j.get("fieldName").equals(new JsonPrimitive(field))) {
                    o = j;
                    break;
                }
            }

            if (o.entrySet().size() == 0) {
                o = new JsonObject();
                o.add("fieldName", new JsonPrimitive(field));
                changes.jsonArray.add(o);
            }

            if (oldValue == null || oldValue.isJsonPrimitive()) {
                o.add("oldValue", oldValue);
            } else {
                o.add("oldValue", new JsonPrimitive(toJsonString(oldValue)));
            }
            return this;
        }


        public Builder removed(JsonObject oldValues) {
            if (oldValues != null) {
                for (Map.Entry<String, JsonElement> entry : oldValues.entrySet()) {
                    removed(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder updated(String field, String oldValue, String newValue) {
            return this.updated(
                    field,
                    oldValue == null ? null : new JsonPrimitive(oldValue),
                    newValue == null ? null : new JsonPrimitive(newValue));
        }
/*
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
*/

        public Builder updated(String field, JsonElement oldValue, JsonElement newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            if (hasChange(oldValue, newValue)) {
                JsonObject o = new JsonObject();
                o.add("fieldName", new JsonPrimitive(field));
                o.add("oldValue", oldValue);
                o.add("newValue", newValue);
                changes.jsonArray.add(o);
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
                    if (child.isJsonPrimitive()) {
                        object.addProperty(fieldName, truncate(toJsonString(child)));
                    } else {
                        traverseAndTruncate(child);
                    }
                }
            } else if (data.isJsonArray()) {
                JsonArray array = data.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement child = array.get(i);
                    if (child.isJsonPrimitive()) {
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
