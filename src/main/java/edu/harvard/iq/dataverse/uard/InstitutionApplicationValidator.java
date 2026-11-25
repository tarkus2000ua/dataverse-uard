package edu.harvard.iq.dataverse.uard;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/** Server validation is independent of browser validation and uploaded MIME headers. */
public final class InstitutionApplicationValidator {
    public static final int MAX_LOGO = 2 * 1024 * 1024;
    public static final List<String> FIELDS = List.of("nameUk", "nameEn", "institutionType", "ownership",
            "affiliation", "division", "website", "descriptionUk", "descriptionEn", "surname",
            "givenName", "patronymic", "position", "phone", "email", "personDescription");
    private static final Set<String> LONG_FIELDS = Set.of("descriptionUk", "descriptionEn", "personDescription");

    private InstitutionApplicationValidator() { }

    public record Validated(Map<String, String> fields, byte[] logo, String logoType) { }

    public static final class Invalid extends Exception {
        private final String field;
        private final String messageKey;
        public Invalid(String field, String messageKey) {
            this.field = field;
            this.messageKey = messageKey;
        }
        public String field() { return field; }
        public String messageKey() { return messageKey; }
    }

    public static Validated validate(Map<String, String> input, boolean consent,
            InputStream logo, long size, String filename) throws Invalid, IOException {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String key : FIELDS) {
            String value = input.getOrDefault(key, "");
            value = value == null ? "" : value.strip();
            if (value.isBlank()) throw new Invalid(key, "errorRequired");
            int maximum = LONG_FIELDS.contains(key) ? 4000 : "website".equals(key) ? 2048 : 255;
            if (value.length() > maximum || value.indexOf('\0') >= 0) throw new Invalid(key, "errorLength");
            fields.put(key, value);
        }
        if (!Set.of("university", "academy", "institute", "college").contains(fields.get("institutionType")))
            throw new Invalid("institutionType", "errorChoice");
        if (!Set.of("state", "municipal", "private").contains(fields.get("ownership")))
            throw new Invalid("ownership", "errorChoice");
        try {
            URI uri = URI.create(fields.get("website"));
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null
                    || uri.getUserInfo() != null) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) {
            throw new Invalid("website", "errorWebsite");
        }
        try {
            String email = fields.get("email");
            InternetAddress address = new InternetAddress(email, true);
            address.validate();
            if (!email.equals(address.getAddress()) || email.contains("\r") || email.contains("\n")
                    || address.getPersonal() != null || !email.substring(email.lastIndexOf('@') + 1).contains("."))
                throw new AddressException();
        } catch (AddressException ex) {
            throw new Invalid("email", "errorEmail");
        }
        if (!consent) throw new Invalid("consent", "errorConsent");
        if (logo == null || size <= 0) throw new Invalid("logo", "errorLogo");
        if (size > MAX_LOGO) throw new Invalid("logo", "logoTooLarge");
        byte[] bytes = logo.readNBytes(MAX_LOGO + 1);
        if (bytes.length > MAX_LOGO) throw new Invalid("logo", "logoTooLarge");
        String type = imageType(bytes, filename);
        return new Validated(Map.copyOf(fields), bytes, type);
    }

    static String imageType(byte[] bytes, String filename) throws Invalid {
        String extension = filename == null ? "" : filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!Set.of("jpg", "jpeg", "png", "gif").contains(extension)) throw new Invalid("logo", "errorLogo");
        try (var stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new Invalid("logo", "errorLogo");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpg".equals(extension)) extension = "jpeg";
                if (!extension.equals(format)) throw new Invalid("logo", "errorLogo");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 16_000_000)
                    throw new Invalid("logo", "errorLogo");
                BufferedImage image = reader.read(0);
                if (image == null) throw new Invalid("logo", "errorLogo");
                return "image/" + format;
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            throw new Invalid("logo", "errorLogo");
        }
    }
}
