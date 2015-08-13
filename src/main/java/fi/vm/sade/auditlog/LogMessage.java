package fi.vm.sade.auditlog;

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

    /**
     * @param messageMapping lokitettava viesti
     */
    private LogMessage(Map<String,String> messageMapping) {
        this.messageMapping = messageMapping;
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder { // 2015-08-05 08:40:20,359
        final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        static final String ID = "id";
        static final String VANHA_ARVO_SUFFIX = ".old_value";
        static final String HAKEMUSOID = "hakemusOid";
        static final String HAKIJAOID = "hakijaOid";
        static final String HAKUOID = "hakuOid";
        static final String HAKUKOHDEOID = "hakukohdeOid";
        static final String HAKIJARYHMAOID = "hakijaryhmaOid";
        static final String VALINTAKOEOID = "valintakoeOid";
        static final String VALINTARYHMAOID = "valintaryhmaOid";
        static final String HAKIJARYHMAVALINTATAPAJONOOID = "hakijaryhmaValintatapajonoOid";
        static final String JARJESTYSKRITEERIOID = "jarjestyskriteeriOid";
        static final String VALINTATAPAJONOOID = "valintatapajonoOid";
        static final String VALINNANVAIHEOID = "valinnanvaiheOid";
        static final String TARJOAJAOID = "tarjoajaOid";
        static final String MESSAGE = "message";
        static final String TIMESTAMP = "timestamp";
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
            return safePut(TIMESTAMP, safeFormat(timestamp));
        }
        public LogMessageBuilder timestamp(Long timestamp) {
            if(timestamp == null) {
                return this;
            }
            return safePut(TIMESTAMP, safeFormat(new Date(timestamp)));
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
            return safePut(ID,id);
        }
        public LogMessageBuilder message(String message) {
            return safePut(MESSAGE,message);
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
            return add(new StringBuilder(key).append(VANHA_ARVO_SUFFIX).toString(), oldValue);
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
