package fi.vm.sade.auditlog;

import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConfigIF;
import org.graylog2.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


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


    void log(Map<String,String> message) {
        StringBuilder b= new StringBuilder("{");
        b.append("\"").append("timestamp").append("\"").append(":\"").append(message.get(LogMessage.LogMessageBuilder.TIMESTAMP)).append("\"").append(",");
        b.append("\"").append("serviceName").append("\"").append(":\"").append(serviceName).append("\"").append(",");
        b.append("\"").append("applicationType").append("\"").append(":\"").append(applicationType).append("\"").append(",");
        boolean firstEntry = true;
        for(Map.Entry<String,String> e : message.entrySet()) {
            if(e.getKey() == null ||e.getValue() == null || LogMessage.LogMessageBuilder.TIMESTAMP.equals(e.getKey())) {
                continue;
            }
            if(!firstEntry) {
                b.append(",");
            } else {
                firstEntry = false;
            }
            b.append("\"").append(e.getKey()).append("\"").append(":\"").append(e.getValue()).append("\"");
        }
        String logLine = b.append("}").toString();
        log.info(logLine);
        SYSLOG.notice(logLine);
    }

    public void log(LogMessage logMessage) {
        log(logMessage.messageMapping);
    }
}
