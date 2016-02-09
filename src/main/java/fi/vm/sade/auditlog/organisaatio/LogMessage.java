package fi.vm.sade.auditlog.organisaatio;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.OPERAATIO;
import static fi.vm.sade.auditlog.organisaatio.OrganisaatioMessageFields.OIDLIST;

public class LogMessage extends AbstractLogMessage {
    public LogMessage(Map<String, String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public LogMessage build() {
            return new LogMessage(mapping);
        }

        public LogMessageBuilder oidList(String oidList) {
            return safePut(OIDLIST, oidList);
        }

        public LogMessageBuilder setOperaatio(OrganisaatioOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}
