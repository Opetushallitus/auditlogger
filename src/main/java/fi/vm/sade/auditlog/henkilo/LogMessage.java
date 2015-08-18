package fi.vm.sade.auditlog.henkilo;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.hakurekisteri.HakuRekisteriMessageFields.OPERAATIO;
import static fi.vm.sade.auditlog.henkilo.HenkiloMessageFields.*;

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

        public LogMessageBuilder kohdeHenkilo(String kohdeHenkiloOid) {
            return safePut(KOHDEHENKILO_OID, kohdeHenkiloOid);
        }

        public LogMessageBuilder kohdeOrganisaatio(String kohdeOrganisaatioOid) {
            return safePut(KOHDEORGANISAATIO_OID, kohdeOrganisaatioOid);
        }

        public LogMessageBuilder lisatieto(String lisatieto) {
            return safePut(LISATIETO, lisatieto);
        }

        public LogMessageBuilder tapahtumatyyppi(String tapahtumatyyppi) {
            return safePut(TAPAHTUMATYYPPI, tapahtumatyyppi);
        }

        public LogMessageBuilder setOperaatio(HenkiloOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}
