package fi.vm.sade.auditlog;

public class LogMessage {
    private final String id;
    private final String message;

    /**
     * @param id Operaation suorittajan tunniste, esim. oid
     * @param message lokitettava viesti
     */
    public LogMessage(String id, String message) {
        this.id = id;
        this.message = message;
    }

    @Override
    public String toString() {
        return "id=\'"+id+"\', message=\'"+message+"\'";
    }
}
