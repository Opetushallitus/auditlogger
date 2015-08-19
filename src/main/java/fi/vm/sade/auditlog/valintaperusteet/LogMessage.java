package fi.vm.sade.auditlog.valintaperusteet;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.*;
import static fi.vm.sade.auditlog.valintaperusteet.ValintaperusteetMessageFields.*;

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

        public LogMessageBuilder hakemusOid(String hakemusOid) {
            return safePut(HAKEMUSOID, hakemusOid);
        }

        public LogMessageBuilder valintakoeOid(String valintakoeOid) {
            return safePut(VALINTAKOEOID, valintakoeOid);
        }

        public LogMessageBuilder jarjestyskriteeriOid(String jarjestyskriteeriOid) {
            return safePut(JARJESTYSKRITEERIOID, jarjestyskriteeriOid);
        }

        public LogMessageBuilder hakuOid(String hakuOid) {
            return safePut(HAKUOID, hakuOid);
        }

        public LogMessageBuilder hakukohdeOid(String hakukohdeOid) {
            return safePut(HAKUKOHDEOID, hakukohdeOid);
        }

        public LogMessageBuilder hakijaOid(String hakijaOid) {
            return safePut(HAKIJAOID, hakijaOid);
        }

        public LogMessageBuilder hakijaryhmaOid(String hakijaryhmaOid) {
            return safePut(HAKIJARYHMAOID, hakijaryhmaOid);
        }

        public LogMessageBuilder hakijaryhmaValintatapajonoOid(String hakijaryhmaValintatapajonoOid) {
            return safePut(HAKIJARYHMAVALINTATAPAJONOOID, hakijaryhmaValintatapajonoOid);
        }

        public LogMessageBuilder valintaryhmaOid(String valintaryhmaOid) {
            return safePut(VALINTARYHMAOID, valintaryhmaOid);
        }

        public LogMessageBuilder valinnanvaiheOid(String valinnanvaiheOid) {
            return safePut(VALINNANVAIHEOID, valinnanvaiheOid);
        }

        public LogMessageBuilder valintatapajonoOid(String valintatapajonoOid) {
            return safePut(VALINTATAPAJONOOID, valintatapajonoOid);
        }

        public LogMessageBuilder tarjoajaOid(String tarjoajaOid) {
            return safePut(TARJOAJAOID, tarjoajaOid);
        }

        public LogMessageBuilder setOperaatio(ValintaperusteetOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}
