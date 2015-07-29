package fi.vm.sade.auditlog;

public class LogMessage {
    private final String oid;
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
        this.oid = id;
        this.palvelunTunniste = palveluTunniste != null ? palveluTunniste.toUpperCase() : null;
        this.tunniste = tunniste != null ? tunniste.toUpperCase() : null;
        this.lokiviesti = lokiviesti;
    }

    @Override
    public String toString() {
        return "oid=\'" + oid + '\'' +
                ", palvelunTunniste=\'" + palvelunTunniste + '\'' +
                ", tunniste=\'" + tunniste + '\'' +
                ", lokiviesti=\'" + lokiviesti + '\'';
    }
}
