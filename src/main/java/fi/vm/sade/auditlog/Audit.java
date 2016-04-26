package fi.vm.sade.auditlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.google.gson.*;

public class Audit {
    private final Logger log;
    private final String serviceName;
    private final String applicationType;
    final Gson gson = new GsonBuilder().serializeNulls().create();

    /**
     * Create an Audit logger for service.
     *
     * @param serviceName name of the service e.g. omatsivut
     * @param applicationType type of the service application e.g. OPISKELIJA
     */
    public Audit(String serviceName, ApplicationType applicationType) {
        this(LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName()), serviceName, applicationType);
    }

    public Audit(Logger log, String serviceName, ApplicationType applicationType) {
        this.log = log;
        this.serviceName = serviceName;
        this.applicationType = applicationType.toString().toLowerCase();
    }

    void log(Map<String,String> message) {
        JsonObject jsonMsg = new JsonObject();
        // Add these first to preserve a certain field order
        addField(jsonMsg, "timestamp", message.get("timestamp"));
        addField(jsonMsg, "serviceName", serviceName);
        addField(jsonMsg, "applicationType", applicationType);
        for (Map.Entry<String, String> entry : message.entrySet()) {
            addField(jsonMsg, entry.getKey(), entry.getValue());
        }
        String logLine = gson.toJson(jsonMsg);
        log.info(logLine);
    }

    private void addField(final JsonObject object, final String key, final String value) {
        if (value == null) {
            object.add(key, null);
        } else {
            object.add(key, new JsonPrimitive(value));
        }
    }

    public void log(AbstractLogMessage logMessage) {
        log(logMessage.getMessageMapping());
    }
}
