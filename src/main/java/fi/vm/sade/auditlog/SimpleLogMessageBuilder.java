package fi.vm.sade.auditlog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SimpleLogMessageBuilder<T extends SimpleLogMessageBuilder<T>> {
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    protected final Map<String, String> mapping;

    public SimpleLogMessageBuilder() {
        this.mapping = new HashMap<>();
        timestamp(new Date().getTime());
    }

    protected String safeFormat(Date d) {
        if (d != null) {
            try {
                return SDF.format(d);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    protected T safePut(String key, String value) {
        if (key != null && value != null) {
            this.mapping.put(key, value);
        }
        return (T) this;
    }

    protected T safePut(String key, Date value) {
        if (key != null && value != null) {
            this.mapping.put(key, safeFormat(value));
        }
        return (T) this;
    }

    public T timestamp(Date timestamp) {
        return safePut(CommonLogMessageFields.TIMESTAMP, safeFormat(timestamp));
    }

    public T timestamp(Long timestamp) {
        if (timestamp == null) {
            return (T) this;
        }
        return safePut(CommonLogMessageFields.TIMESTAMP, safeFormat(new Date(timestamp)));
    }

    public T id(String id) {
        return safePut(CommonLogMessageFields.ID, id);
    }

    public T message(String message) {
        return safePut(CommonLogMessageFields.MESSAGE, message);
    }

    public T addAll(Map<String, String> mapping) {
        if (mapping != null) {
            this.mapping.putAll(mapping);
        }
        return (T) this;
    }

    public <V> T add(String key, V value, V oldValue) {
        add(key, value);
        return add(key + CommonLogMessageFields.VANHA_ARVO_SUFFIX, oldValue);
    }

    public <V> T add(String key, V value) {
        if (value == null) {
            return (T) this;
        }
        if (value instanceof Date) {
            safePut(key, safeFormat((Date) value));
        } else {
            safePut(key, value.toString());
        }
        return (T) this;
    }
}
