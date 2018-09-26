### Auditlogger

Auditlogikomponentti logitukseen. Logittaa syslogiin ja slf4j-apiin.

Syslog4j-kirjastot:

* https://github.com/Graylog2/syslog4j-graylog2
* https://github.com/twall/jna

## Käyttöönotto

Maven: 
``` 
    <dependency>
        <groupId>fi.vm.sade</groupId>
        <artifactId>auditlogger</artifactId>
        <version>versio-pom.xml-tiedostossa</version>
    </dependency>
```
       
SBT: 
```
    "fi.vm.sade" % "auditlogger" % "versio-pom.xml-tiedostossa"
```

### Käyttö

Jokaiselle palvelulle on oma paketti, jonka alta löytyy LogMessage-luokka, jota tulisi käyttää. Oheisessa esimerkissä 
käytetään valintaperusteet-repon versiota. 

Java: 
```
    import static fi.vm.sade.auditlog.valintaperusteet.LogMessage.builder;

    String serviceName = "omatsivut";
    Audit audit = new Audit(serviceName, ApplicationType.OPISKELIJA);
    LogMessage logMessage = builder().id("testuser").hakukohdeOid("1.2.246.562.20.68888036172").message("hello").build();
    audit.log(logMessage);

    // Tilasiirtymän logitus
    LogMessage logMessage = builder()
        .id("testuser")
        .hakemusOid("1.2.246.562.20.68888036172")
        .add("hakemuksentila", HYVAKSYTTY)
        .add("valintatuloksentila", VASTAANOTETTU_SITOVASTI, EHDOLLISESTI_VASTAANOTETTU)
        .message("Hakijan vastaanottotila päivitetty sitovaksi")
        .build();
    audit.log(logMessage);
```

Syslog:

`{"id":"testuser","message":"test message","hakukohdeOid":"1.2.246.562.20.68888036172","serviceName":"test","applicationType":"opiskelija"}

lokitiedosto (konfiguraatiosta riippuen, esim /logs/auditlog_omatsivut.log):

`{"id":"testuser","message":"test message","hakukohdeOid":"1.2.246.562.20.68888036172","serviceName":"test","applicationType":"opiskelija"}

### Kehitys

OS X:
Ajamalla `mvn test` Audit kirjoittaa lokia tiedostoihin:

* /var/logs/system.log
* ./auditlog_test.log (Huom! konfiguroitu logback_test.xml:ssä, testit siivoavat tämän tiedoston pois.)

Syslogia voi ihmetellä myös OS X:n Console-sovelluksella.


### Logback-konfiguraatioesimerkki

Audit lokittaa myös käyttäen slf4j fasadia, johon voi konfiguroida toteutuksen sovellluksessa.
Alla Logback-esimerkki.

Maven pom.xml: 
``` 
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.12</version>
    </dependency>
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
                <pattern>%msg%n</pattern>
            </encoder>
        </appender>
        <logger name="fi.vm.sade.auditlog.Audit" level="INFO" additivity="false">
            <appender-ref ref="ROLLING" />
        </logger>
    </configuration>
```

### log4j-kofiguraatioesimerkki

Maven pom.xml:

```
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.12</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-log4j12</artifactId>
      <version>1.7.12</version>
    </dependency>`
```

log4j.properties:

```
    log4j.additivity.fi.vm.sade.auditlog.Audit=false
    log4j.logger.fi.vm.sade.auditlog.Audit=INFO, AUDIT
    log4j.appender.AUDIT=org.apache.log4j.RollingFileAppender
    log4j.appender.AUDIT.File=${user.home}/tomcat/logs/auditlog_valintalaskentakoostepalvelu.log
    log4j.appender.AUDIT.Append=true
    log4j.appender.AUDIT.MaxFileSize=20MB
    log4j.appender.AUDIT.MaxBackupIndex=20
    log4j.appender.AUDIT.layout=org.apache.log4j.PatternLayout
    log4j.appender.AUDIT.layout.ConversionPattern=%m%n
    log4j.appender.AUDIT.encoding=UTF-8
```
