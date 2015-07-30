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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String palveluTunniste;
        private String tunniste;
        private String lokiviesti;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setPalveluTunniste(String palveluTunniste) {
            this.palveluTunniste = palveluTunniste;
            return this;
        }

        public Builder setTunniste(String tunniste) {
            this.tunniste = tunniste;
            return this;
        }

        public Builder setLokiviesti(String lokiviesti) {
            this.lokiviesti = lokiviesti;
            return this;
        }

        public LogMessage build() {
            return new LogMessage(id, palveluTunniste, tunniste, lokiviesti);
        }
    }
}
