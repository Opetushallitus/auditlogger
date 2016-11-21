package fi.vm.sade.auditlog;

import fi.vm.sade.auditlog.valintaperusteet.LogMessage;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static fi.vm.sade.auditlog.valintaperusteet.LogMessage.builder;
import static java.util.concurrent.TimeUnit.*;

public class HeartbeatDaemon implements Runnable {
    private static HeartbeatDaemon instance = null;
    private static final long MINUTES_BETWEEN_HEARTBEATS = 10;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(newDaemonThreadFactory());
    private final CopyOnWriteArrayList<Audit> loggers = new CopyOnWriteArrayList<>();
    private final String hostname = System.getProperty("HOSTNAME", "");
    private final Date bootTime = new Date();
    private HeartbeatDaemon() {
        scheduler.scheduleAtFixedRate(this,MINUTES_BETWEEN_HEARTBEATS,MINUTES_BETWEEN_HEARTBEATS,MINUTES);
        final Runnable onShutdown = new Runnable() {
            @Override
            public void run() {
                for(Audit audit: loggers) {
                    log(audit, "Server shutting down!");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(onShutdown));

    }

    public String getHostname() {
        return hostname;
    }

    public static synchronized HeartbeatDaemon getInstance() {
        if(instance == null){
            instance = new HeartbeatDaemon();
        }
        return instance;
    }

    public Date getBootTime() {
        return bootTime;
    }

    public void register(Audit audit) {
        this.loggers.add(audit);
        log(audit, "Server started!");
    }
    private static void log(Audit audit, final String message) {
        audit.log(builder().message(message).build());
    }
    @Override
    public void run() {
        for(Audit audit: loggers) {
            log(audit, "Alive!");
        }
    }

    private static ThreadFactory newDaemonThreadFactory() {
        return new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true); // <- doesnt block application shutdown
                return t;
            }
        };
    }
}
