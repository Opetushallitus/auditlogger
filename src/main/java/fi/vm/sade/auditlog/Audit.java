package fi.vm.sade.auditlog;

import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Audit {
    private static final SyslogIF SYSLOG = Syslog.getInstance("unix_syslog");
    private final Logger log;
    private String serviceName;

    public Audit() {
        this("", LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName()));
    }

    public Audit(String serviceName) {
        this(serviceName.toUpperCase(), LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName()));
    }

    Audit(String serviceName, Logger log) {
        this.log = log;
        this.serviceName = serviceName.toUpperCase();
    }

    public void log(String message) {
        final String msg = "["+serviceName+"]: " + message;
        log.info(msg);
        SYSLOG.notice(msg);
    }

    public void log(LogMessage logMessage) {
        log(logMessage.toString());
    }
}
