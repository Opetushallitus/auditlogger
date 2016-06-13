package fi.vm.sade.auditlog.oikeustulkkirekisteri;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.OPERAATIO;
import static fi.vm.sade.auditlog.oikeustulkkirekisteri.OikeustulkkiMessageFields.HENKILO_OID;
import static fi.vm.sade.auditlog.oikeustulkkirekisteri.OikeustulkkiMessageFields.OIKEUSTULKKI_ID;

/**
 * User: tommiratamaa
 * Date: 13.6.2016
 * Time: 13.31
 */
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

        public LogMessageBuilder henkiloOid(String oid) {
            return safePut(HENKILO_OID, oid);
        }

        public LogMessageBuilder oikeustulkkiId(Long id) {
            return safePut(OIKEUSTULKKI_ID, ""+id);
        }
        
        public LogMessageBuilder setOperaatio(OikeustulkkiOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}