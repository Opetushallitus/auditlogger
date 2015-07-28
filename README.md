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

###Syslogin enablointi OS X v10.7 eteen päin

```
cd /System/Library/LaunchDaemons
sudo /usr/libexec/PlistBuddy -c "add :Sockets:NetworkListener dict" com.apple.syslogd.plist
sudo /usr/libexec/PlistBuddy -c "add :Sockets:NetworkListener:SockServiceName string syslog" com.apple.syslogd.plist
sudo /usr/libexec/PlistBuddy -c "add :Sockets:NetworkListener:SockType string dgram" com.apple.syslogd.plist
sudo launchctl unload com.apple.syslogd.plist
sudo launchctl load com.apple.syslogd.plist`
```
Pre 10.7

```
<!--
        Un-comment the following lines to enable the network syslog protocol listener.
-->
                <key>NetworkListener</key>
                <dict>
                        <key>SockServiceName</key>
                        <string>syslog</string>
                        <key>SockType</key>
                        <string>dgram</string>
                </dict>
        </dict>
</dict>
</plist>
```
