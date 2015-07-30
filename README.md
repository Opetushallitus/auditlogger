### Auditlogger

Auditlogikomponentti logitukseen. Logittaa tiedoston lisäksi syslogiin.

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
String logFileDir = "/logs";
Audit log = new Audit(serviceName, logFileDir);
LogMessage logMessage = LogMessage.builder()
    .setId("ID")
    .setPalveluTunniste("omatsivut")
    .setTunniste("opiskelija")
    .setLokiviesti("Opiskelija kirjautui sisään")
    .build();
audit.log(logMessage);
// Viesti menee syslog:iin ja tiedostoon /logs/auditlog_omatsivut.log
```
