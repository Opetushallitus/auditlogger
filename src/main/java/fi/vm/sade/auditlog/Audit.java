package fi.vm.sade.auditlog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class Audit {

    private static final int VERSION = 1;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
    }

    private final Clock clock;
    private final Date bootTime;
    private final String hostname;
    private final HeartbeatDaemon heartbeat;
    private final AtomicInteger logSeq;
    private final Logger log;
    private final String serviceName;
    private final String applicationType;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    /**
     * Create an Audit logger for service.
     *
     * @param serviceName name of the service e.g. omatsivut
     * @param applicationType type of the service application e.g. OPISKELIJA
     */
    public Audit(String serviceName, ApplicationType applicationType) {
        this(
                LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName()),
                serviceName,
                applicationType,
                System.getProperty("HOSTNAME", ""),
                HeartbeatDaemon.getInstance(),
                new AtomicInteger(0),
                new Clock() {
                    @Override
                    public Date wallClockTime() {
                        return new Date();
                    }
                }
        );
    }

    public Audit(Logger log, String serviceName, ApplicationType applicationType, String hostname, HeartbeatDaemon heartbeat, AtomicInteger logSeq, Clock clock) {
        this.clock = clock;
        this.bootTime = clock.wallClockTime();
        this.hostname = hostname;
        this.log = log;
        this.serviceName = serviceName;
        this.applicationType = applicationType.toString().toLowerCase();
        this.heartbeat = heartbeat;
        this.logSeq = logSeq;
        heartbeat.register(this);
    }

    private JsonObject commonFields() {
        JsonObject json = new JsonObject();

        json.addProperty("version", VERSION);
        json.addProperty("logSeq", logSeq.getAndIncrement());
        json.addProperty("bootTime", SDF.format(this.bootTime));
        json.addProperty("hostname", this.hostname);

        synchronized (SDF) {
            json.addProperty("timestamp", SDF.format(clock.wallClockTime()));
        }

        json.addProperty("serviceName", serviceName);
        json.addProperty("applicationType", applicationType);

        return json;
    }

    public void logStarted() {
        JsonObject json = commonFields();
        json.addProperty("message", "started");
        log.info(gson.toJson(json));
    }

    public void logHeartbeat() {
        JsonObject json = commonFields();
        json.addProperty("message", "alive");
        log.info(gson.toJson(json));
    }

    public void logStopped() {
        JsonObject json = commonFields();
        json.addProperty("message", "stopped");
        log.info(gson.toJson(json));
    }

    public void log(User user,
                    Operation operation,
                    Target target,
                    Changes changes) {
        JsonObject json = commonFields();
        json.add("user", user.asJson());
        json.addProperty("operation", operation.name());
        json.add("target", target.asJson());
        json.add("changes", changes.asJson());
        log.info(gson.toJson(json));
    }
}
