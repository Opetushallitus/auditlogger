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

    private JsonArray jsonArray = new JsonArray();

    private Changes() { }

    public static <T> Changes addedDto(T dto) {
        return new Changes.Builder().added(gson.toJsonTree(dto)).build();
    }

    public static <T> Changes updatedDto(T dtoAfter, T dtoBefore) {
        JsonElement afterJson = gson.toJsonTree(dtoAfter);
        JsonElement beforeJson = gson.toJsonTree(dtoBefore);
        return new Changes.Builder().jsonDiffToChanges(beforeJson, afterJson).build();
    }

    public static <T> Changes deleteDto(T dto) {
        return new Changes.Builder().removed(gson.toJsonTree(dto)).build();
    }

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
            return r;
        }

        public Builder added(String field, String newValue) {
            return this.added(field,
                    newValue == null ? null : new JsonPrimitive(newValue));
        }

        public Builder added(JsonElement newValue) {
            if (!newValue.isJsonNull()) {
                JsonObject j = new JsonObject();
                j.add("newValue", new JsonPrimitive(toJsonString(newValue)));
                changes.jsonArray.add(j);
            }
            return this;
        }

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

        public Builder removed(JsonElement oldValue) {
            if (!oldValue.isJsonNull()) {
                JsonObject j = new JsonObject();
                j.add("oldValue", new JsonPrimitive(toJsonString(oldValue)));
                changes.jsonArray.add(j);
            }
            return this;
        }

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
            JsonPatch patchArray = withCombinedPatchOps(beforeJson, jsonPatchFactory.create(beforeJson, afterJson));
            JsonElement current = beforeJson;
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
                        JsonElement oldValue = Util.getJsonElementByPath(current, prettyPath);
                        removed(prettyPath, toJsonString(oldValue));
                        break;
                    }
                    case "replace": {
                        JsonElement oldValue = Util.getJsonElementByPath(current, prettyPath);
                        JsonElement newValue = ((ReplaceOperation) absOperation).data;
                        updated(prettyPath, toJsonString(oldValue), toJsonString(newValue));
                        break;
                    }
                    case "move": {
                        String prettyFromPath = prettify(((MoveOperation) absOperation).from);
                        JsonElement oldValue = Util.getJsonElementByPath(current, prettyFromPath);
                        removed(prettyFromPath, toJsonString(oldValue));

                        JsonElement newValue = Util.getJsonElementByPath(afterJson, prettyPath);
                        added(prettyPath, newValue);
                        break;
                    }
                    default: throw new IllegalArgumentException(String.format("Unknown operation %s in %s . before: %s . after: %s"
                            , operation, absOperation.path, beforeJson, afterJson));
                }
                current = absOperation.apply(current);
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
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else if (element.isJsonNull()) {
            return element.toString();
        }
        else {
            return gson.toJson(element);
        }
    }

    private static JsonPatch withCombinedPatchOps(JsonElement json, JsonPatch patch) {
        // JsonPatchFactory occasionally returns suboptimal patches,
        // where for example a replace of an array element can be expressed
        // as consecutive add and remove (or the other way around) to the same index.
        // Try to combine pairs of such operations into one.

        // Note: this helper is not designed to handle all possible cases,
        // just the more obvious cases to begin with!
        // This means only combining *consecutive* adds and removes to the same
        // *leaf* element of a path.
        try {
            JsonPatch modified = new JsonPatch();
            AbsOperation prev = null;
            for (Iterator<AbsOperation> it = patch.iterator(); it.hasNext(); ) {
                AbsOperation current = it.next();
                if (prev != null) {
                    AbsOperation combined = combine(json, prev, current);
                    if (combined != null) {
                        // Add combined operation into patch, discard previous and current operation.
                        modified.add(combined);
                        current = null;
                    }
                }
                prev = current;
            }
            if (prev != null) {
                modified.add(prev);
            }
            return modified;
        } catch (Exception e) {
            return patch;
        }
    }

    private static AbsOperation combine(JsonElement json, AbsOperation a, AbsOperation b) {
        try {
            String aOp = a.getOperationName();
            String bOp = b.getOperationName();
            boolean removeThenAdd = "remove".equals(aOp) && "add".equals(bOp);
            boolean addThenRemove = "add".equals(aOp) && "remove".equals(bOp);
            boolean compatibleOperations = removeThenAdd || addThenRemove;
            if (!compatibleOperations) {
                return null;
            }
            String aPath = prettify(a.path);
            String bPath = prettify(b.path);

            // Ensure paths share same prefix up to leaf
            int idxOfLastPeriod = aPath.lastIndexOf('.');
            if (bPath.lastIndexOf('.') != idxOfLastPeriod) {
                return null;
            }
            String pathToParentOfLeaf = idxOfLastPeriod > 0 ? aPath.substring(0, idxOfLastPeriod) : null;
            if (pathToParentOfLeaf != null && !pathToParentOfLeaf.equals(bPath.substring(0, idxOfLastPeriod))) {
                return null;
            }
            // Ensure path leaves point to array elements
            JsonElement parent = pathToParentOfLeaf == null ? json : Util.getJsonElementByPath(json, pathToParentOfLeaf, true);
            if (!parent.isJsonArray()) {
                return null;
            }
            // Only attempt to combine operations if they modify the same array index
            String aLeafPart = pathToParentOfLeaf == null ? aPath : aPath.substring(idxOfLastPeriod + 1);
            String bLeafPart = pathToParentOfLeaf == null ? bPath : bPath.substring(idxOfLastPeriod + 1);
            int aLeafIdx = Integer.parseInt(aLeafPart);
            int bLeafIdx = Integer.parseInt(bLeafPart);
            if (removeThenAdd && aLeafIdx == bLeafIdx) {
                AddOperation addOp = (AddOperation)b;
                return new ReplaceOperation(addOp.path, addOp.data);
            } else if (addThenRemove && aLeafIdx == bLeafIdx - 1) {
                // Adding to array shifts elements after added position to right by 1.
                // Only combine the add+remove into a replace if the remove targets
                // the element that used to be at the same index as the addition.
                AddOperation addOp = (AddOperation)a;
                return new ReplaceOperation(addOp.path, addOp.data);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

