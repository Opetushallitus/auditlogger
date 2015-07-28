### Auditlogger

Auditlogikomponentti logitukseen. Käyttää sysloggeria logitukseen.

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
LogMessage logMessage = new LogMessageBuilder()
                         .setId("ID")
                         .setPalveluTunniste("omatsivut")
                         .setTunniste("opiskelija")
                         .setLokiviesti("Opiskelija kirjautui sisään")
                         .build()
audit.log(logMessage);
```
