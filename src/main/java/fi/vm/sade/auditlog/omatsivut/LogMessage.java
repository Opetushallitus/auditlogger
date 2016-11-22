package fi.vm.sade.auditlog.omatsivut;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;
import java.util.Map;

import static fi.vm.sade.auditlog.omatsivut.OmatsivutMessageFields.*;

public class LogMessage extends AbstractLogMessage {
    public LogMessage(Map<String, String> messageMapping) {
        super(messageMapping);
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public LogMessage build() {
            return new LogMessage(mapping);
        }

        public LogMessageBuilder userOid(String oid) {
            return safePut(USER_OID, oid);
        }

        public LogMessageBuilder hakukohdeOid(String hakukohdeOid) {
            return safePut(HAKUKOHDE_OID, hakukohdeOid);
        }

        public LogMessageBuilder hakuOid(String hakuOid) {
            return safePut(HAKU_OID, hakuOid);
        }

        public LogMessageBuilder hakemusOid(String hakemusOid) {
            return safePut(HAKEMUS_OID, hakemusOid);
        }

    }
}
