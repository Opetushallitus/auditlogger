package fi.vm.sade.auditlog.oppijanumerorekisteri;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.OPERAATIO;
import static fi.vm.sade.auditlog.oppijanumerorekisteri.OppijanumerorekisteriMessageFields.*;

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

        public LogMessageBuilder kohdehenkiloOid(String oid) {
            return safePut(KOHDEHENKILO_OID, oid);
        }

        public LogMessageBuilder kohdeorganisaatioOid(String oid) {
            return safePut(KOHDEORGANISAATIO_OID, oid);
        }

        public LogMessageBuilder lisatieto(String lisatieto) {
            return safePut(LISATIETO, lisatieto);
        }

        public LogMessageBuilder muutettuUusi(String uusi) {
            return safePut(MUUTETTU_UUSI, uusi);
        }

        public LogMessageBuilder muutettuVanha(String vanha) {
            return safePut(MUUTETTU_VANHA, vanha);
        }

        public LogMessageBuilder setOperaatio(OppijanumerorekisteriOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}
