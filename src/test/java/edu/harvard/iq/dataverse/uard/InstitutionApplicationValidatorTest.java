package edu.harvard.iq.dataverse.uard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionApplicationValidatorTest {
    static Map<String, String> fields() {
        Map<String, String> fields = new HashMap<>();
        InstitutionApplicationValidator.FIELDS.forEach(key -> fields.put(key, "Example"));
        fields.put("institutionType", "university");
        fields.put("ownership", "state");
        fields.put("website", "https://example.edu.ua/");
        fields.put("email", "person@example.edu.ua");
        return fields;
    }
    static byte[] image(String format) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, bytes);
        return bytes.toByteArray();
    }
    static InstitutionApplicationValidator.Validated validate(Map<String, String> fields, boolean consent,
            byte[] logo, String name) throws Exception {
        return InstitutionApplicationValidator.validate(fields, consent, new ByteArrayInputStream(logo), logo.length, name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpeg", "gif"})
    void acceptsRealImagesAndTrimsFields(String format) throws Exception {
        var input = fields();
        input.put("nameUk", "  Університет  ");
        var result = validate(input, true, image(format), "logo." + format);
        assertEquals("Університет", result.fields().get("nameUk"));
        assertEquals("image/" + format, result.logoType());
    }

    @Test void rejectsEveryMissingRequiredField() throws Exception {
        for (String key : InstitutionApplicationValidator.FIELDS) {
            var input = fields();
            input.put(key, " \n ");
            var ex = assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(input, true, image("png"), "logo.png"));
            assertEquals(key, ex.field());
            assertEquals("errorRequired", ex.messageKey());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "example.edu.ua", "https://", "https://user:password@example.edu.ua", "ftp://example.edu.ua"})
    void rejectsUnsafeOrInvalidWebsites(String website) throws Exception {
        var input = fields(); input.put("website", website);
        assertEquals("website", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> validate(input, true, image("png"), "logo.png")).field());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "a@b", "Name <user@example.org>", "a@example.org\r\nBcc: x@example.org", "a@example.org,b@example.org"})
    void rejectsInvalidEmailAddresses(String email) throws Exception {
        var input = fields(); input.put("email", email);
        assertEquals("email", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> validate(input, true, image("png"), "logo.png")).field());
    }

    @Test void validatesChoicesLengthAndConsent() throws Exception {
        for (String field : new String[]{"institutionType", "ownership", "nameUk", "descriptionUk"}) {
            var input = fields(); input.put(field, "x".repeat(field.equals("descriptionUk") ? 4001 : 256));
            assertEquals(field, assertThrows(InstitutionApplicationValidator.Invalid.class,
                    () -> validate(input, true, image("png"), "logo.png")).field());
        }
        var choices = fields(); choices.put("ownership", "other");
        assertEquals("errorChoice", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> validate(choices, true, image("png"), "logo.png")).messageKey());
        assertEquals("consent", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> validate(fields(), false, image("png"), "logo.png")).field());
    }

    @Test void rejectsSpoofedCorruptEmptyAndOversizeFiles() throws Exception {
        assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(fields(), true, "<script/>".getBytes(java.nio.charset.StandardCharsets.UTF_8), "logo.png"));
        assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(fields(), true, image("png"), "logo.jpg"));
        assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(fields(), true, image("png"), "logo.svg"));
        assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(fields(), true, new byte[0], "logo.png"));
        byte[] oversized = new byte[InstitutionApplicationValidator.MAX_LOGO + 1];
        assertEquals("logoTooLarge", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> validate(fields(), true, oversized, "logo.png")).messageKey());
        // Do not trust the multipart size supplied by a caller.
        assertEquals("logoTooLarge", assertThrows(InstitutionApplicationValidator.Invalid.class,
                () -> InstitutionApplicationValidator.validate(fields(), true, new ByteArrayInputStream(oversized), 1, "logo.png")).messageKey());
        byte[] corrupt = java.util.Arrays.copyOf(image("png"), 33);
        assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(fields(), true, corrupt, "logo.png"));
    }

    @Test void rejectsHugeImageDimensionsBeforeDecoding() throws Exception {
        // ImageIO creates a small compressed file with 20M pixels; server must reject its dimensions.
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(5000, 4000, BufferedImage.TYPE_BYTE_BINARY), "png", output);
        assertThrows(InstitutionApplicationValidator.Invalid.class, () -> validate(fields(), true, output.toByteArray(), "logo.png"));
    }
}
