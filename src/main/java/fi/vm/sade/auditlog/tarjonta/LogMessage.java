package fi.vm.sade.auditlog.tarjonta;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

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

        public LogMessageBuilder setOperation(TarjontaOperation operation) {
            return safePut(OPERAATIO, operation.name());
        }

        public LogMessageBuilder setResource(TarjontaResource resource) {
            return safePut(TarjontaMessageFields.RESOURCE, resource.name());
        }

        public LogMessageBuilder setResourceOid(String oid) {
            return safePut(TarjontaMessageFields.RESOURCE_OID, oid);
        }

        public LogMessageBuilder setDelta(String deltaAsJson) {
            return safePut(TarjontaMessageFields.DELTA, StringEscapeUtils.escapeJson(deltaAsJson));
        }
    }
}
