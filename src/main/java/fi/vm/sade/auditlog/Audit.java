package fi.vm.sade.auditlog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class Audit {
    private static final int VERSION = 1;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    public static final String TYPE_ALIVE = "alive";
    private static final String TYPE_LOG = "log";

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
    }

    private final Clock clock;
    private final Date bootTime;
    private final String hostname;
    private final HeartbeatDaemon heartbeat;
    private final AtomicInteger logSeq;
    private final Logger logger;
    private final String serviceName;
    private final String applicationType;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Create an Audit logger for service.
     *
     * @param serviceName name of the service e.g. omatsivut
     * @param applicationType type of the service application e.g. OPISKELIJA
     */
    public Audit(Logger logger, String serviceName, ApplicationType applicationType) {
        this(
                logger,
                serviceName,
                applicationType,
                System.getProperty("HOSTNAME", ""),
                HeartbeatDaemon.getInstance(),
                new Clock() {
                    @Override
                    public Date wallClockTime() {
                        return new Date();
                    }
                }
        );
    }

    public Audit(Logger logger, String serviceName, ApplicationType applicationType, String hostname, HeartbeatDaemon heartbeat, Clock clock) {
        this.clock = clock;
        this.bootTime = clock.wallClockTime();
        this.hostname = hostname;
        this.logger = logger;
        this.serviceName = serviceName;
        this.applicationType = applicationType.toString().toLowerCase();
        this.heartbeat = heartbeat;
        this.logSeq = new AtomicInteger(0);
        heartbeat.register(this);
    }

    private ObjectNode commonFields(String type) {

        ObjectNode json = mapper.createObjectNode();

        json.put("version", VERSION);
        json.put("logSeq", logSeq.getAndIncrement());
        json.put("type", type);

        synchronized (SDF) {
            json.put("bootTime", SDF.format(this.bootTime));
            json.put("hostname", this.hostname);
            json.put("timestamp", SDF.format(clock.wallClockTime()));
        }

        json.put("serviceName", serviceName);
        json.put("applicationType", applicationType);

        return json;
    }

    public void logStarted() {
        ObjectNode json = commonFields(TYPE_ALIVE);
        json.put("message", "started");
        logger.log(json.toString());
    }

    public void logHeartbeat() {
        ObjectNode json = commonFields(TYPE_ALIVE);
        json.put("message", "alive");
        logger.log(json.toString());
    }

    public void logStopped() {
        ObjectNode json = commonFields(TYPE_ALIVE);
        json.put("message", "stopped");
        logger.log(json.toString());
    }

    public void log(User user, Operation operation, Target target, ArrayNode changes) {
        ObjectNode json = commonFields(TYPE_LOG);
        json.set("user", user.asJson());
        try {
            json.put("operation", operation.name());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Operation is required");
        }
        json.set("target", target.asJson());
        json.set("changes", changes);
        logger.log(json.toString());
    }

    public void log(User user,
                    Operation operation,
                    Target target,
                    Changes changes) {
        this.log(user, operation, target, changes.asJsonArray());
    }
}
