package fi.vm.sade.auditlog.hakurekisteri;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.hakurekisteri.HakuRekisteriMessageFields.OPERAATIO;
import static fi.vm.sade.auditlog.hakurekisteri.HakuRekisteriMessageFields.RESOURCE_NAME;

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

        public LogMessageBuilder setOperaatio(HakuRekisteriOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }

        public LogMessageBuilder setResourceName(String resourceName) {
            return safePut(RESOURCE_NAME, resourceName);
        }
    }
}
