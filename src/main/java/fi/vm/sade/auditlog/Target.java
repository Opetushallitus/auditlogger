package fi.vm.sade.auditlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Target {
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode json = mapper.createObjectNode();

    private Target() { }

    public ObjectNode asJson() {
        return this.json;
    }

    public static class Builder {
        private Target target;

        public Builder() {
            this.target = new Target();
        }

        public Target build() {
            Target r = this.target;
            this.target = new Target();
            return r;
        }

        public Builder setField(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            this.target.json.put(name, value);
            return this;
        }
    }
}
