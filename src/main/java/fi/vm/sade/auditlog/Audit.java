package fi.vm.sade.auditlog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Audit {
    private Logger log = LoggerFactory.getLogger(fi.vm.sade.auditlog.Audit.class.getName());
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

    public void log(String msg) {
        log.info("["+serviceName+"]: " + msg);
    }

    private void configureFileLogger(String file) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern("%date %level %logger{10} [%file:%line] %msg%n");
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setAppend(true);
        fileAppender.setName("file");
        fileAppender.setFile(file);
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)log;
        logger.addAppender(fileAppender);
        logger.setLevel(Level.ALL);
        logger.setAdditive(true);
    }
    
}
