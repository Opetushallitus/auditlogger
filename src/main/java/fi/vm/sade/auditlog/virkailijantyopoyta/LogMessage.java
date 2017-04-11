package fi.vm.sade.auditlog.virkailijantyopoyta;


import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import static fi.vm.sade.auditlog.CommonLogMessageFields.OPERAATIO;
import static fi.vm.sade.auditlog.virkailijantyopoyta.VirkailjianTyopoytaMessageFields.*;

import java.util.Map;

public class LogMessage extends AbstractLogMessage {

    public LogMessage(Map<String, String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public LogMessage build(){
            return new LogMessage(mapping);
        }

        public LogMessageBuilder virkailijaOid(String oid){
            return safePut(VIRKAILIJA_OID, oid);
        }

        public LogMessageBuilder releaseId(String releaseId){
            return safePut(RELEASE_ID, releaseId);
        }

        public LogMessageBuilder setOperaatio(VirkailijanTyopoytaOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}
