package fi.vm.sade.auditlog.valintarekisteri;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.*;
import static fi.vm.sade.auditlog.valintarekisteri.ValintarekisteriFields.*;

public class LogMessage extends AbstractLogMessage {
    public LogMessage(final Map<String, String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public LogMessage build() {
            return new LogMessage(mapping);
        }

        public LogMessageBuilder setOperaatio(final ValintarekisteriOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }

        public LogMessageBuilder setKohdeHenkiloOid(final String kohdeHenkiloOid) {
            return safePut(KOHDEHENKILO_OID, kohdeHenkiloOid);
        }

        public LogMessageBuilder setHakukohdeOid(final String hakukohdeOid) {
            return safePut(HAKUKOHDE_OID, hakukohdeOid);
        }

        public LogMessageBuilder setIlmoittajaHenkiloOid(final String ilmoittajaHenkiloOid) {
            return safePut(ILMOITTAJAHENKILO_OID, ilmoittajaHenkiloOid);
        }

        public LogMessageBuilder setIlmoitushetki(final Long ilmoitushetki) {
            return safePut(ILMOITUSHETKI, ilmoitushetki.toString());
        }
    }
}
