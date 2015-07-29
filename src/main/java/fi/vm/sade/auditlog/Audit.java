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
    }

    public Audit(String serviceName, String file) {
        this.serviceName = serviceName.toUpperCase();
        log = configureLoggerWithFileLogger(file);
    }

    Audit(String serviceName, Logger log) {
        this.log = log;
        this.serviceName = serviceName.toUpperCase();
    }

    public void log(String message) {
        final String msg = "[" + serviceName + "]: " + message;
        log.info(msg);
        SYSLOG.notice(msg);
    }

    public void log(LogMessage logMessage) {
        log(logMessage.toString());
    }

    private Logger configureLoggerWithFileLogger(String file) {
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
        rollingFileAppender.setFile(file);
        rollingFileAppender.setEncoder(patternLayoutEncoder);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern("auditlog_" + serviceName + ".%i.txt.zip");
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

        return logger;
    }
}
