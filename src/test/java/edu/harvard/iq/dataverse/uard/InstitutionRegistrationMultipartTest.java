package edu.harvard.iq.dataverse.uard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionRegistrationMultipartTest {
    private static final String TYPE = "multipart/form-data; boundary=uard-test";
    @Test void preservesUnicodeAndBinaryLogo() throws Exception {
        var fields = InstitutionApplicationValidatorTest.fields();
        fields.put("nameUk", "Український університет");
        byte[] logo = InstitutionApplicationValidatorTest.image("png");
        var parsed = InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(
                InstitutionRegistrationServletTest.multipart(fields, logo)), TYPE);
        assertEquals(fields, parsed.fields());
        assertArrayEquals(logo, parsed.logo());
        assertEquals("logo.png", parsed.filename());
    }
    @Test void enforcesRequestBoundWithoutTrustingContentLength() {
        class LargeStream extends InputStream {
            int count;
            @Override public int read() { count++; return 'x'; }
        }
        var stream = new LargeStream();
        assertEquals("logoTooLarge", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> InstitutionRegistrationMultipart.parse(stream, TYPE)).messageKey());
        assertEquals(InstitutionRegistrationMultipart.MAX_REQUEST + 1, stream.count);
    }
    @Test void rejectsInvalidMimeTypeDuplicateFieldsAndMissingClosingBoundary() throws Exception {
        byte[] bytes = InstitutionRegistrationServletTest.multipart(InstitutionApplicationValidatorTest.fields(), InstitutionApplicationValidatorTest.image("png"));
        assertThrows(Exception.class, () -> InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(bytes), "text/plain"));
        var out = new java.io.ByteArrayOutputStream();
        out.write("--uard-test\r\nContent-Disposition: form-data; name=\"nameUk\"\r\n\r\nDuplicate\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(bytes);
        assertThrows(IOException.class, () -> InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(out.toByteArray()), TYPE));
        byte[] truncated = java.util.Arrays.copyOf(bytes, bytes.length - 17);
        assertThrows(Exception.class, () -> InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(truncated), TYPE));
    }
    @Test void boundsPartCountAndFieldBytesBeforeValidation() throws Exception {
        var fields = new HashMap<String, String>();
        for (int index = 0; index < 21; index++) fields.put("extra" + index, "x");
        byte[] bytes = InstitutionRegistrationServletTest.multipart(fields, InstitutionApplicationValidatorTest.image("png"));
        assertThrows(IOException.class, () -> InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(bytes), TYPE));
        fields.clear(); fields.put("descriptionUk", "x".repeat(16001));
        byte[] longField = InstitutionRegistrationServletTest.multipart(fields, InstitutionApplicationValidatorTest.image("png"));
        assertEquals("errorLength", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(longField), TYPE)).messageKey());
    }
    @Test void boundsDecodedFileBeforeImageValidation() throws Exception {
        byte[] bytes = InstitutionRegistrationServletTest.multipart(new HashMap<>(), new byte[InstitutionApplicationValidator.MAX_LOGO + 1]);
        assertEquals("logoTooLarge", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> InstitutionRegistrationMultipart.parse(new ByteArrayInputStream(bytes), TYPE)).messageKey());
    }
}
