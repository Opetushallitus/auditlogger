package fi.vm.sade.auditlog.haku;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.*;
import static fi.vm.sade.auditlog.haku.HakuMessageFields.*;

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

        public LogMessageBuilder hakemusOid(String hakemusOid) {
            return safePut(HAKEMUSOID, hakemusOid);
        }

        public LogMessageBuilder hakuOid(String hakuOid) {
            return safePut(HAKUOID, hakuOid);
        }

        public LogMessageBuilder hakukohdeOid(String hakukohdeOid) {
            return safePut(HAKUKOHDEOID, hakukohdeOid);
        }

        public LogMessageBuilder hakukohderyhmaOid(String hakukohderyhmaOid) {
            return safePut(HAKUKOHDERYHMAOID, hakukohderyhmaOid);
        }

        public LogMessageBuilder setOperaatio(HakuOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}
