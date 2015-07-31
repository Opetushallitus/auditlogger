package fi.vm.sade.auditlog;

public class LogMessage {
    private final String id;
    private final String userIndentity;
    private final String message;

    /**
     * @param id esim. oid
     * @param userIndentity esim. virkailija/opiskelija
     * @param message lokitettava viesti
     */
    public LogMessage(String id, String userIndentity, String message) {
        this.id = id;
        this.userIndentity = userIndentity != null ? userIndentity : null;
        this.message = message;
    }

    @Override
    public String toString() {
        return "id=\'"+id+"\', userIdentity=\'"+userIndentity+"\', message=\'"+message+"\'";
    }
}
