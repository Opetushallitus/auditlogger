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
        static final String VANHA_ARVO_SUFFIX = ".vanha_arvo";
        static final String HAKEMUSOID = "hakemusOid";
        static final String HENKILOOID = "henkiloOid";
        static final String HAKUOID = "hakuOid";
        static final String HAKUKOHDEOID = "hakukohdeOid";
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
        public LogMessageBuilder timestamp(Date timestamp) {
            this.mapping.put(TIMESTAMP, SDF.format(timestamp));
            return this;
        }
        public LogMessageBuilder timestamp(long timestamp) {
            this.mapping.put(TIMESTAMP, SDF.format(new Date(timestamp)));
            return this;
        }
        public LogMessageBuilder hakemusOid(String hakemusOid) {
            this.mapping.put(HAKEMUSOID, hakemusOid);
            return this;
        }
        public LogMessageBuilder hakuOid(String hakuOid) {
            this.mapping.put(HAKUOID, hakuOid);
            return this;
        }
        public LogMessageBuilder hakukohdeOid(String hakukohdeOid) {
            this.mapping.put(HAKUKOHDEOID, hakukohdeOid);
            return this;
        }
        public LogMessageBuilder henkiloOid(String henkiloOid) {
            this.mapping.put(HENKILOOID, henkiloOid);
            return this;
        }
        public LogMessageBuilder id(String id) {
            this.mapping.put(ID,id);
            return this;
        }
        public LogMessageBuilder message(String message) {
            this.mapping.put(MESSAGE,message);
            return this;
        }
        public LogMessageBuilder valinnanvaiheOid(String valinnanvaiheOid) {
            this.mapping.put(VALINNANVAIHEOID, valinnanvaiheOid);
            return this;
        }
        public LogMessageBuilder valintatapajonoOid(String valintatapajonoOid) {
            this.mapping.put(VALINTATAPAJONOOID, valintatapajonoOid);
            return this;
        }
        public LogMessageBuilder tarjoajaOid(String tarjoajaOid) {
            this.mapping.put(TARJOAJAOID, tarjoajaOid);
            return this;
        }
        public <T> LogMessageBuilder add(String key, T value) {
            this.mapping.put(key, value.toString());
            return this;
        }
        public <T> LogMessageBuilder add(String key, T value, T oldValue) {
            this.mapping.put(key, value.toString());
            this.mapping.put(new StringBuilder(key).append(VANHA_ARVO_SUFFIX).toString(), oldValue.toString());
            return this;
        }
        public LogMessage build() {
            return new LogMessage(mapping);
        }
    }


}
