package fi.vm.sade.auditlog;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class SimpleLogMessageBuilder<T extends SimpleLogMessageBuilder<T>> {
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    protected final Map<String, String> mapping;

    public SimpleLogMessageBuilder() {
        this.mapping = new HashMap<>();
        timestamp(new Date());
    }

    protected String safeFormat(Date d) {
        if (d != null) {
            try {
                SDF.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
                return SDF.format(d);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    protected T safePut(String key, String value) {
        if (key != null) {
            this.mapping.put(key, value);
        }
        return (T) this;
    }

    protected T safePut(String key, Date value) {
        if (key != null) {
            this.mapping.put(key, safeFormat(value));
        }
        return (T) this;
    }

    public T timestamp(Date timestamp) {
        return safePut(CommonLogMessageFields.TIMESTAMP, safeFormat(timestamp));
    }

    public T timestamp(Long timestamp) {
        if (timestamp == null) {
            return safePut(CommonLogMessageFields.TIMESTAMP, (String) null);
        } else {
            return safePut(CommonLogMessageFields.TIMESTAMP, safeFormat(new Date(timestamp)));
        }
    }

    public T id(String id) {
        return safePut(CommonLogMessageFields.ID, id);
    }

    public T message(String message) {
        return safePut(CommonLogMessageFields.MESSAGE, message);
    }

    public T changes(String message) {
        return safePut(CommonLogMessageFields.CHANGES, message);
    }

    /**
     * @deprecated Do not use, we don't want random key-value pairs in the logs. Instead use the message method if you
     * want to add almost free form data to the log.
     */
    @Deprecated
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
        if (value instanceof Date) {
            safePut(key, safeFormat((Date) value));
        } else if (value != null) {
            safePut(key, value.toString());
        } else {
            safePut(key, (String) null);
        }
        return (T) this;
    }

    /**
     * Adds a free form object to message field. Uses Gson to convert map into a string representation.
     * @param object
     */
    public T messageJson(Map<String, ? extends Object> object) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(object);
        return this.message(jsonStr);
    }

    /**
     * Adds a free form object to changes field. Uses Gson to convert map into a string representation.
     * @param object
     */
    public T changesJson(Map<String, ? extends Object> object) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(object);
        return this.changes(jsonStr);
    }
}
