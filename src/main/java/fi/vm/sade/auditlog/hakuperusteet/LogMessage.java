package fi.vm.sade.auditlog.hakuperusteet;

import fi.vm.sade.auditlog.AbstractLogMessage;
import fi.vm.sade.auditlog.SimpleLogMessageBuilder;

import java.util.Date;
import java.util.Map;

import static fi.vm.sade.auditlog.CommonLogMessageFields.*;
import static fi.vm.sade.auditlog.hakuperusteet.HakuPerusteetMessageFields.*;
import static fi.vm.sade.auditlog.hakuperusteet.HakuPerusteetOperation.*;

public class LogMessage extends AbstractLogMessage {
    public LogMessage(Map<String,String> messageMapping) {
        super(messageMapping);
    }

    public static LogMessageBuilder builder() {
        return new LogMessageBuilder();
    }

    public static class LogMessageBuilder extends SimpleLogMessageBuilder<LogMessageBuilder> {
        public LogMessage build() {
            return new LogMessage(mapping);
        }

        public LogMessageBuilder oppijaHenkiloOid(String oppijaHenkiloOid) {
            return safePut(OPPIJAHENKILOOID, oppijaHenkiloOid);
        }

        public LogMessageBuilder virkailijaHenkiloOid(String virkailijaHenkiloOid) {
            return safePut(VIRKAILIJAHENKILOOID, virkailijaHenkiloOid);
        }

        public LogMessageBuilder email(String email) {
            return safePut(EMAIL, email);
        }

        public LogMessageBuilder hakuOid(String hakuOid) {
            return safePut(HAKUOID, hakuOid);
        }

        public LogMessageBuilder hakukohdeOid(String hakukohdeOid) {
            return safePut(HAKUKOHDEOID, hakukohdeOid);
        }

        public LogMessageBuilder firstName(String firstName) {
            return safePut(FIRSTNAME, firstName);
        }

        public LogMessageBuilder lastName(String lastName) {
            return safePut(LASTNAME, lastName);
        }

        public LogMessageBuilder birthDate(Date birthDate) {
            return safePut(BIRTHDATE, birthDate);
        }

        public LogMessageBuilder personId(String personId) {
            return safePut(PERSONID, personId);
        }

        public LogMessageBuilder gender(String gender) {
            return safePut(GENDER, gender);
        }

        public LogMessageBuilder nativeLanguage(String nativeLanguage) {
            return safePut(NATIVELANGUAGE, nativeLanguage);
        }

        public LogMessageBuilder nationality(String birthDate) {
            return safePut(NATIONALITY, birthDate);
        }

        public LogMessageBuilder educationLevel(String educationLevel) {
            return safePut(EDUCATIONLEVEL, educationLevel);
        }

        public LogMessageBuilder educationCountry(String educationCountry) {
            return safePut(EDUCATIONCOUNTRY, educationCountry);
        }

        public LogMessageBuilder reference(String reference) {
            return safePut(REFERENCE, reference);
        }

        public LogMessageBuilder orderNumber(String orderNumber) {
            return safePut(ORDERNUMBER, orderNumber);
        }

        public LogMessageBuilder paymCallId(String paymCallId) {
            return safePut(PAYMCALLID, paymCallId);
        }

        public LogMessageBuilder setOperaatio(HakuPerusteetOperation operaatio) {
            return safePut(OPERAATIO, operaatio.name());
        }
    }
}