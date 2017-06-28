package fi.vm.sade.auditlog;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;

public class CommonLogMessageFieldsTest {

    @Test
    public void fieldNameTest() throws Exception {
        Field[] declaredFields = CommonLogMessageFields.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                String value = (String) field.get(null);
                assertFalse(String.format("Auditlogger message keys can't contain dots, offending field %s value %s",
                        field.getName(), value),
                        value.contains("."));
            }
        }
    }
}