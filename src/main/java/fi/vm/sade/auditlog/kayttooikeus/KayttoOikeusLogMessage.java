package fi.vm.sade.auditlog.kayttooikeus;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.OPERAATIO;

public class KayttoOikeusLogMessage extends AbstractLogMessage {

    private static final String KOHDETUNNISTE = "kohdeTunniste";
    private static final String NEW_VALUE = "uusiArvo";
    private static final String LISATIETO = "lisatieto";

    private KayttoOikeusLogMessage(Map<String, String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public KayttoOikeusLogMessage build() {
            return new KayttoOikeusLogMessage(mapping);
        }

        public LogMessageBuilder newValue(String value) {
            return safePut(NEW_VALUE, value);
        }

        public LogMessageBuilder kohdeTunniste(String tunniste) {
            return safePut(KOHDETUNNISTE, tunniste);
        }

        public LogMessageBuilder lisatieto(String lisatieto) {
            return safePut(LISATIETO, lisatieto);
        }

        public LogMessageBuilder setOperaatio(KayttoOikeusOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}