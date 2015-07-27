package fi.vm.sade.auditlog;

public class LogMessageBuilder {
    private String id;
    private String palveluTunniste;
    private String tunniste;
    private String lokiviesti;

    public LogMessageBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public LogMessageBuilder setPalveluTunniste(String palveluTunniste) {
        this.palveluTunniste = palveluTunniste;
        return this;
    }

    public LogMessageBuilder setTunniste(String tunniste) {
        this.tunniste = tunniste;
        return this;
    }

    public LogMessageBuilder setLokiviesti(String lokiviesti) {
        this.lokiviesti = lokiviesti;
        return this;
    }

    public LogMessage build() {
        return new LogMessage(id, palveluTunniste, tunniste, lokiviesti);
    }
}