package edu.harvard.iq.dataverse.uard;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.ContentDisposition;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** Bounded multipart parsing using the mail MIME library already packaged with Dataverse.
 * Avoids Payara's eager multipart parsing before CDI context activation/error handling. */
public final class InstitutionRegistrationMultipart {
    public static final int MAX_REQUEST = 3 * 1024 * 1024;
    private InstitutionRegistrationMultipart() { }
    public record Parsed(Map<String, String> fields, byte[] logo, String filename) { }

    public static Parsed parse(InputStream input, String contentType)
            throws IOException, MessagingException, InstitutionApplicationValidator.Invalid {
        ContentType type = new ContentType(contentType == null ? "" : contentType);
        String boundary = type.getParameter("boundary");
        if (!"multipart/form-data".equalsIgnoreCase(type.getBaseType()) || boundary == null
                || boundary.isBlank() || boundary.length() > 70) throw new IOException("Invalid multipart type");
        byte[] body = input.readNBytes(MAX_REQUEST + 1);
        if (body.length > MAX_REQUEST) throw new InstitutionApplicationValidator.Invalid("logo", "logoTooLarge");
        var multipart = new MimeMultipart(new ByteArrayDataSource(body, contentType));
        int count = multipart.getCount();
        if (count > 20 || !multipart.isComplete()) throw new IOException("Invalid multipart structure");
        Map<String, String> fields = new HashMap<>();
        var names = new HashSet<String>();
        byte[] logo = new byte[0];
        String filename = null;
        for (int index = 0; index < count; index++) {
            var part = multipart.getBodyPart(index);
            String[] header = part.getHeader("Content-Disposition");
            if (header == null || header.length != 1) throw new IOException("Missing part disposition");
            var disposition = new ContentDisposition(header[0]);
            String name = disposition.getParameter("name");
            if (!"form-data".equalsIgnoreCase(disposition.getDisposition()) || name == null || !names.add(name))
                throw new IOException("Invalid or duplicate part");
            try (var stream = part.getInputStream()) {
                if ("logo".equals(name)) {
                    logo = stream.readNBytes(InstitutionApplicationValidator.MAX_LOGO + 1);
                    if (logo.length > InstitutionApplicationValidator.MAX_LOGO)
                        throw new InstitutionApplicationValidator.Invalid("logo", "logoTooLarge");
                    filename = part.getFileName();
                } else {
                    if (!(InstitutionApplicationValidator.FIELDS.contains(name)
                            || "consent".equals(name) || "captcha".equals(name) || "token".equals(name))
                            || part.getFileName() != null) throw new IOException("Unexpected part");
                    byte[] value = stream.readNBytes(16001);
                    if (value.length > 16000) throw new InstitutionApplicationValidator.Invalid(name, "errorLength");
                    fields.put(name, StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(value)).toString());
                }
            }
        }
        return new Parsed(Map.copyOf(fields), logo, filename);
    }
}
