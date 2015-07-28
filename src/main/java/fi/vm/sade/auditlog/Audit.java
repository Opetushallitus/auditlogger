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
    private Logger log = LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName());
    private static final SyslogIF SYSLOG = Syslog.getInstance("unix_syslog");
    private final String serviceName;

    public Audit() {
        this.serviceName = "";
    }

    public Audit(String serviceName, String file) {
        this.serviceName = serviceName.toUpperCase();
        configureFileLogger(file);
    }

    Audit(String serviceName, Logger log) {
        this.serviceName = serviceName.toUpperCase();
        this.log = log;
    }

    public void log(String message) {
        final String msg = "[" + serviceName + "]: " + message;
        log.info(msg);
        SYSLOG.notice(msg);
    }

    void log(LogMessage logMessage) {
        log.info(logMessage.toString());
        SYSLOG.notice(logMessage.toString());
    }

    private void configureFileLogger(String file) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setCharset(Charset.forName("UTF-8"));
        patternLayoutEncoder.setPattern("%date %level %logger{10} [%file:%line] %msg%n");
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = setupFileAppender(file, loggerContext, patternLayoutEncoder);

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) log;
        logger.addAppender(rollingFileAppender);
        logger.setLevel(Level.ALL);
        logger.setAdditive(true);
    }

    private RollingFileAppender<ILoggingEvent> setupFileAppender(String file, LoggerContext loggerContext, PatternLayoutEncoder patternLayoutEncoder) {
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setName("file");
        rollingFileAppender.setFile(file);
        rollingFileAppender.setEncoder(patternLayoutEncoder);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern("auditlog.%i.txt.zip");
        rollingPolicy.start();

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setMaxFileSize("5MB");
        triggeringPolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);

        rollingFileAppender.start();
        return rollingFileAppender;
    }
}
