package fi.vm.sade.auditlog;

import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Audit {
    private static final SyslogIF SYSLOG = Syslog.getInstance("unix_syslog");
    private final Logger log;
    private final String serviceName;
    private final String applicationType;

    /**
     * Create an Audit logger for service.
     *
     * @param serviceName name of the service e.g. omatsivut
     * @param applicationType type of the service application e.g. OPISKELIJA
     */
    public Audit(String serviceName, ApplicationType applicationType) {
        this(LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName()), serviceName, applicationType);
    }

    /**
     * Package private constructor for testing
     */
    Audit(Logger log, String serviceName, ApplicationType applicationType) {
        SyslogConfigIF syslogConf = SYSLOG.getConfig();
        syslogConf.setCharSet("UTF-8");
        syslogConf.setIdent(applicationType.toString().toLowerCase() + "-app");
        this.log = log;
        this.serviceName = serviceName;
        this.applicationType = applicationType.toString().toLowerCase();
    }


    void log(String message) {
        final String msg = "["+serviceName+"] " + message;
        log.info(applicationType + "-app: " + msg);
        SYSLOG.notice(msg);
    }

    public void log(LogMessage logMessage) {
        log(logMessage.toString());
    }
}
