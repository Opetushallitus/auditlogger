package fi.vm.sade.auditlog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;


public class Audit {
    private static final SyslogIF SYSLOG = Syslog.getInstance("unix_syslog");
    private final Logger log;
    private String serviceName;

    public Audit() {
        log = LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName());
        this.serviceName = "";
        logInitMessage();
    }


    public Audit(String serviceName, String logFileDir) {
        log = LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName());
        this.serviceName = serviceName.toUpperCase();
        logInitMessage();
        configureLoggerWithFileLogger(logFileDir);
    }

    Audit(String serviceName, Logger log) {
        this.log = log;
        this.serviceName = serviceName.toUpperCase();
        logInitMessage();
    }

    public void log(String message) {
        final String msg = "[" + serviceName + "]: " + message;
        log.info(msg);
        SYSLOG.notice(msg);
    }

    public void log(LogMessage logMessage) {
        log(logMessage.toString());
    }

    private void logInitMessage() {
        log("Initializing audit logging");
    }

    private void configureLoggerWithFileLogger(String logFileDir) {
        final String auditLogFileName = "auditlog" + (!serviceName.isEmpty() ? "_"+serviceName.toLowerCase() : "") + ".log";
        final String auditLogFileFullPath = (logFileDir.endsWith("/") ? logFileDir : logFileDir + "/") + auditLogFileName;
        log("Configuring audit logging to: " + auditLogFileFullPath);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName());
        LoggerContext loggerContext = logger.getLoggerContext();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setCharset(Charset.forName("UTF-8"));
        patternLayoutEncoder.setPattern("%date %level %logger{10} [%file:%line] %msg%n");
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setName("file");
        rollingFileAppender.setFile(auditLogFileFullPath);
        rollingFileAppender.setEncoder(patternLayoutEncoder);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(auditLogFileName + ".%i.log.zip");
        rollingPolicy.start();

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setMaxFileSize("5MB");
        triggeringPolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
        rollingFileAppender.start();

        logger.addAppender(rollingFileAppender);
        logger.setLevel(Level.ALL);
        logger.setAdditive(true);
    }
}
