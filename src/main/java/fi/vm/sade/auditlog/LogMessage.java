package fi.vm.sade.auditlog;

public class LogMessage {
    private final String id;
    private final String palvelunTunniste;
    private final String tunniste;
    private final String lokiviesti;

    /**
     * @param id esim. oid
     * @param palveluTunniste palvelunnimi esim. omatsivut
     * @param tunniste esim. virkailija/opiskelija
     * @param lokiviesti lokitettava viesti
     */
    public LogMessage(String id, String palveluTunniste, String tunniste, String lokiviesti) {
        this.id = id;
        this.palvelunTunniste = palveluTunniste != null ? palveluTunniste.toUpperCase() : null;
        this.tunniste = tunniste != null ? tunniste.toUpperCase() : null;
        this.lokiviesti = lokiviesti;
    }

    public LogMessage(String id) {
        this.id = id;
        this.palvelunTunniste = null;
        this.tunniste = null;
        this.lokiviesti = null;
    }

    public LogMessage withPalvelunTunniste(String palvelunTunniste) {
        return new LogMessage(this.id, palvelunTunniste, this.tunniste, this.lokiviesti);
    }

    public LogMessage withTunniste(String tunniste) {
        return new LogMessage(this.id, this.palvelunTunniste, tunniste, this.lokiviesti);
    }

    public LogMessage withLokiviesti(String lokiviesti) {
        return new LogMessage(this.id, this.palvelunTunniste, this.tunniste, lokiviesti);
    }

    @Override
    public String toString() {
        return "id=\'" + id + '\'' +
                ", palvelunTunniste=\'" + palvelunTunniste + '\'' +
                ", tunniste=\'" + tunniste + '\'' +
                ", lokiviesti=\'" + lokiviesti + '\'';
    }
}
