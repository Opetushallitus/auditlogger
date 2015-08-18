package fi.vm.sade.auditlog.henkilo;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.CommonLogMessageFields;

import static fi.vm.sade.auditlog.henkilo.HenkiloMessageFields.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;

public class LogMessage extends AbstractLogMessage {
    public LogMessage(Map<String,String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder {
        final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        private final Map<String,String> mapping;

        public LogMessageBuilder() {
            this.mapping = new HashMap<>();
            timestamp(new Date().getTime());
        }
        private String safeFormat(Date d) {
            if(d != null) {
                try {
                    return SDF.format(d);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return null;
        }
        private LogMessageBuilder safePut(String key, String value) {
            if(key != null && value != null) {
                this.mapping.put(key, value);
            }
            return this;
        }
        public LogMessageBuilder timestamp(Date timestamp) {
            return safePut(CommonLogMessageFields.TIMESTAMP, safeFormat(timestamp));
        }
        public LogMessageBuilder timestamp(Long timestamp) {
            if(timestamp == null) {
                return this;
            }
            return safePut(CommonLogMessageFields.TIMESTAMP, safeFormat(new Date(timestamp)));
        }
        public LogMessageBuilder id(String id) {
            return safePut(CommonLogMessageFields.ID,id);
        }

        public LogMessageBuilder message(String message) {
            return safePut(CommonLogMessageFields.MESSAGE,message);
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

        public LogMessageBuilder addAll(Map<String,String> mapping) {
            if(mapping != null) {
                this.mapping.putAll(mapping);
            }
            return this;
        }
        public <T> LogMessageBuilder add(String key, T value, T oldValue) {
            add(key,value);
            return add(key + CommonLogMessageFields.VANHA_ARVO_SUFFIX, oldValue);
        }
        public <T> LogMessageBuilder add(String key, T value) {
            if(value == null) {
                return this;
            }
            if(value instanceof Date) {
                safePut(key, safeFormat((Date)value));
            } else {
                safePut(key, value.toString());
            }
            return this;
        }
        public LogMessage build() {
            return new LogMessage(mapping);
        }
    }
}
