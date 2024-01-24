package fi.vm.sade.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jsonpatch.diff.JsonDiff;

import java.util.Iterator;
import java.util.Map;

public final class Changes {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ArrayNode jsonArray = mapper.createArrayNode();

    private Changes() { }

    public static <T> Changes addedDto(T dto) {
        return new Changes.Builder().added(mapper.valueToTree(dto)).build();
    }

    public static <T> Changes updatedDto(T dtoAfter, T dtoBefore) {
        JsonNode afterJson = mapper.valueToTree(dtoAfter);
        JsonNode beforeJson = mapper.valueToTree(dtoBefore);
        return new Changes.Builder().jsonDiffToChanges(beforeJson, afterJson).build();
    }

    public static <T> Changes deleteDto(T dto) {
        return new Changes.Builder().removed(mapper.valueToTree(dto)).build();
    }

    public ArrayNode asJsonArray() {
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
            return r;
        }

        public Builder added(String field, String newValue) {
            return this.added(field,
                    newValue == null ? null : TextNode.valueOf(newValue));
        }

        public Builder added(JsonNode newValue) {
            if (!newValue.isNull()) {
                ObjectNode j = mapper.createObjectNode();
                j.put("newValue", newValue.asText());
                changes.jsonArray.add(j);
            }
            return this;
        }

        public Builder added(String field, JsonNode newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }

            ObjectNode o = mapper.createObjectNode();
            for (int i = 0; i < changes.jsonArray.size(); i++) {
                ObjectNode j = (ObjectNode) changes.jsonArray.get(i);
                if (j.get("fieldName").equals(TextNode.valueOf(field))) {
                    o = j;
                    break;
                }
            }

            if (!o.elements().hasNext()) {
                o = mapper.createObjectNode();
                o.put("fieldName", field);
                changes.jsonArray.add(o);
            }
            if (newValue == null || newValue.isValueNode()) {
                o.set("newValue", newValue);
            } else {
                o.put("newValue", newValue.toString());

            }
            return this;
        }

        public Builder added(ObjectNode newValues) {
            if (newValues != null) {
                Iterator<Map.Entry<String, JsonNode>> entries = newValues.fields();
                while (entries.hasNext()) {
                    Map.Entry<String, JsonNode> entry = entries.next();
                    added(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder removed(String field, String oldValue) {
            return this.removed(field,
                    oldValue == null ? null : TextNode.valueOf(oldValue));
        }

        public Builder removed(JsonNode oldValue) {
            if (!oldValue.isNull()) {
                ObjectNode j = mapper.createObjectNode();
                j.put("oldValue", oldValue.asText());
                changes.jsonArray.add(j);
            }
            return this;
        }

        public Builder removed(String field, JsonNode oldValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }

            ObjectNode o = mapper.createObjectNode();
            for(int i = 0; i < changes.jsonArray.size(); i++) {
                ObjectNode j = (ObjectNode) changes.jsonArray.get(i);
                if(j.get("fieldName").equals(TextNode.valueOf(field))) {
                    o = j;
                    break;
                }
            }

            if (!o.elements().hasNext()) {
                o = mapper.createObjectNode();
                o.put("fieldName", field);
                changes.jsonArray.add(o);
            }

            if (oldValue == null || oldValue.isValueNode()) {
                o.set("oldValue", oldValue);
            } else {
                o.put("oldValue", oldValue.toString());
            }
            return this;
        }

        public Builder removed(ObjectNode oldValues) {
            if (oldValues != null) {
                Iterator<Map.Entry<String, JsonNode>> entries = oldValues.fields();
                while (entries.hasNext()) {
                    Map.Entry<String, JsonNode> entry = entries.next();
                    removed(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder updated(String field, String oldValue, String newValue) {
            return this.updated(
                    field,
                    oldValue == null ? null : TextNode.valueOf(oldValue),
                    newValue == null ? null : TextNode.valueOf(newValue));
        }

        public Builder updated(String field, JsonNode oldValue, JsonNode newValue) {
            if (field == null) {
                throw new IllegalArgumentException("Field name is required");
            }
            if (hasChange(oldValue, newValue)) {
                ObjectNode o = mapper.createObjectNode();
                o.put("fieldName", field);
                o.set("oldValue", oldValue);
                o.set("newValue", newValue);
                changes.jsonArray.add(o);
            }
            return this;
        }
        private Builder jsonDiffToChanges(JsonNode beforeJson, JsonNode afterJson) {
            ArrayNode patch = (ArrayNode) JsonDiff.asJson(beforeJson, afterJson);
            JsonNode current = beforeJson;
            for (JsonNode operation : patch) {
                String op = operation.get("op").asText();
                String path = operation.get("path").asText();
                JsonNode value = operation.get("value");
                String prettyPath = prettify(path);
                switch (op) {
                    case "add": {
                        added(prettyPath, value);
                        break;
                    }
                    case "remove": {
                        removed(prettyPath, current.at(path));
                        break;
                    }
                    case "replace": {
                        updated(prettyPath, current.at(path), value);
                        break;
                    }
                    case "move": {
                        String fromPath = operation.get("from").asText();
                        removed(prettify(fromPath), current.at(fromPath));
                        added(prettyPath, afterJson.at(path));
                        break;
                    }
                    default: throw new IllegalArgumentException(
                            String.format(
                                "Unknown operation %s in %s . before: %s . after: %s",
                                op,
                                path,
                                beforeJson,
                                afterJson
                            )
                        );
                }
            }
            return this;
        }

        private boolean hasChange(JsonNode oldValue, JsonNode newValue) {
            return null == oldValue ? null != newValue : !oldValue.equals(newValue);
        }
    }

    private static String prettify(String path) {
        return path.substring(1).replaceAll("/", ".");
    }
}
