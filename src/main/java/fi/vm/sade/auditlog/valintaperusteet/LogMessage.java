package fi.vm.sade.auditlog.valintaperusteet;

import fi.vm.sade.auditlog.CommonLogMessageFields;
import static fi.vm.sade.auditlog.valintaperusteet.ValintaperusteetMessageFields.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;

/**
 * Usage: LogMessage logMessage = LogMessage.builder()
 *          .id("testuser")
 *          .hakemusOid("1.2.246.562.20.68888036172")
 *          .add("valintatuloksentila", "VASTAANOTTANUT", "EHDOLLINEN")
 *          .message("Update tila EHDOLLINEN -> VASTAANOTTANUT").build();
 */
public class LogMessage {
    final Map<String,String> messageMapping;

    private LogMessage(Map<String,String> messageMapping) {
        this.messageMapping = messageMapping;
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
        public LogMessageBuilder id(String id) {
            return safePut(CommonLogMessageFields.ID,id);
        }
        public LogMessageBuilder message(String message) {
            return safePut(CommonLogMessageFields.MESSAGE,message);
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

    public Map<String, String> getMessageMapping() {
        return messageMapping;
    }
}
