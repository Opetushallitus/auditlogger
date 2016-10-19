package fi.vm.sade.auditlog.oti;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.OPERAATIO;

public class LogMessage extends AbstractLogMessage {
    public LogMessage(Map<String,String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public LogMessage build() {
            return new LogMessage(mapping);
        }

        public LogMessageBuilder setOperation(OTIOperation operation) {
            return safePut(OPERAATIO, operation.name());
        }

        public LogMessageBuilder setResource(OTIResource resource) {
            return safePut(OTIMessageFields.RESOURCE, resource.name());
        }

        public LogMessageBuilder setDelta(String delta) {
            return safePut(OTIMessageFields.DELTA, delta);
        }
    }
}
