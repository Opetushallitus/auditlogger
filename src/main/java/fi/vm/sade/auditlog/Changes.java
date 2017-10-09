package fi.vm.sade.auditlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class Changes {
    private JsonObject json = new JsonObject();

    private Changes() { }

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

        private boolean hasChange(JsonElement oldValue, JsonElement newValue) {
            return null == oldValue ? null != newValue : !oldValue.equals(newValue);
        }
    }
}
