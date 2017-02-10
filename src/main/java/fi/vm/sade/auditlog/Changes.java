package fi.vm.sade.auditlog;

import com.google.gson.JsonObject;

import java.util.Map;

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
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            JsonObject o = new JsonObject();
            o.addProperty("newValue", newValue);
            changes.json.add(field, o);
            return this;
        }

        public Builder removed(String field, String oldValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            JsonObject o = new JsonObject();
            o.addProperty("oldValue", oldValue);
            changes.json.add(field, o);
            return this;
        }

        public Builder updated(String field, String oldValue, String newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            if (!oldValue.equals(newValue)) {
                JsonObject o = new JsonObject();
                o.addProperty("oldValue", oldValue);
                o.addProperty("newValue", newValue);
                changes.json.add(field, o);
            }
            return this;
        }
    }
}
