package fi.vm.sade.auditlog;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Audit {
    private final Logger log;

    public Audit(String name) {
        log = createLogger(name, "./logs/auditlog.txt");
    }

    Audit(Logger log) {
        this.log = log;
    }

    public void log(String service, String msg) {
        log.info(service.toUpperCase() + ":" + msg, "");
    }

    private Logger createLogger(String name, String file) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();

        patternLayoutEncoder.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();

        ConsoleAppender logConsoleAppender = new ConsoleAppender();
        logConsoleAppender.setContext(loggerContext);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(patternLayoutEncoder);
        logConsoleAppender.start();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
        logger.addAppender(logConsoleAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false); /* set to true if root should log too */

        return logger;
    }
}
