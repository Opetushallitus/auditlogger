package fi.vm.sade.auditlog;

import java.util.Map;

public abstract class AbstractLogMessage {
    private final Map<String,String> messageMapping;

    public AbstractLogMessage(Map<String,String> messageMapping) {
        this.messageMapping = messageMapping;
    }

    public Map<String, String> getMessageMapping() {
        return messageMapping;
    }
}
