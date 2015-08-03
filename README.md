### Auditlogger

Auditlogikomponentti logitukseen. Logittaa syslogiin ja slf4j-apiin.

Syslog4j-kirjastot:

* https://github.com/Graylog2/syslog4j-graylog2
* https://github.com/twall/jna

##Käyttöönotto

Maven: 
``` 
    <dependency>
        <groupId>fi.vm.sade</groupId>
        <artifactId>auditlogger</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```
       
SBT: 
```
    "fi.vm.sade" % "auditlogger" % "1.0-SNAPSHOT"
```

###Käyttö

Java: 
```
    String serviceName = "omatsivut";
    Audit audit = new Audit(serviceName);
    LogMessage logMessage = new LogMessage("ID", "opiskelija", "Opiskelija kirjautui sisään");
    audit.log(logMessage);
    // Viesti menee syslog:iin ja tiedostoon /logs/auditlog_omatsivut.log
```

###slf4j Logback konfiguraatio

Audit lokittaa myös käyttäen slf4j fasadia, johon voi konfiguroida toteutuksen sovellluksessa.
Alla Logback-esimerkki.

Maven: 
``` 
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.1.3</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-core</artifactId>
        <version>1.1.3</version>
    </dependency>
```

src/main/resources/logback.xml:
```
    <configuration>
        <appender name="ROLLING" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>auditlog.log</file>
            <append>true</append>
            <rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy">
                <fileNamePattern>auditlog.%i.log.zip</fileNamePattern>
                <minIndex>1</minIndex>
                <maxIndex>10</maxIndex>
            </rollingPolicy>
            <triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
                <maxFileSize>5MB</maxFileSize>
            </triggeringPolicy>
            <encoder>
                <pattern>%date AUDIT %msg%n</pattern>
            </encoder>
        </appender>
        <logger name="fi.vm.sade.auditlog.Audit" level="INFO">
            <appender-ref ref="ROLLING" />
        </logger>
    </configuration>
```

###log4j-kofiguraatioesimerkki

```
log4j.logger.fi.vm.sade.auditlog.Audit=INFO, file

log4j.appender.file=org.apache.log4j.RollingFileAppender

log4j.appender.file.File=auditlog.log
log4j.appender.file.MaxFileSize=5MB
log4j.appender.file.MaxBackupIndex=10
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%date AUDIT %m%n
```
