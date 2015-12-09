package fi.vm.sade.auditlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Audit {
    private final Logger log;
    private final String serviceName;
    private final String applicationType;
    final Gson gson = new Gson();

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
        JsonObject object = new JsonObject();
        // Add these first to preserve a certain field order
        object.add("timestamp", s(message.get("timestamp")));
        object.add("serviceName", s(serviceName));
        object.add("applicationType", s(applicationType));
        for (Map.Entry<String, String> entry : message.entrySet()) {
            object.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        String logLine = gson.toJson(object);
        log.info(logLine);
    }

    public void log(AbstractLogMessage logMessage) {
        log(logMessage.getMessageMapping());
    }

    private JsonElement s(String s) {
        return new JsonPrimitive(s);
    }
}
