package com.iot.management.service;

import com.cloudinary.Cloudinary;
import com.iot.management.exception.DeviceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private final Cloudinary cloudinary;
    private final boolean configured;

    public CloudinaryService(@Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        configured = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
        cloudinary = new Cloudinary(Map.of("cloud_name", cloudName, "api_key", apiKey,
            "api_secret", apiSecret, "secure", true));
    }

    public boolean isConfigured() { return configured; }
    public record UploadedImage(String url, String publicId) {}

    public UploadedImage upload(MultipartFile file) {
        validateImage(file);
        if (!configured) throw new DeviceException(HttpStatus.SERVICE_UNAVAILABLE,
            "Chưa cấu hình Cloudinary. Bạn vẫn có thể lưu thiết bị không kèm ảnh.");
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of(
                "folder", "iot-devices", "resource_type", "image",
                "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif"}));
            return new UploadedImage((String) result.get("secure_url"), (String) result.get("public_id"));
        } catch (IOException | RuntimeException ex) {
            throw new DeviceException(HttpStatus.BAD_GATEWAY, "Không thể tải ảnh lên Cloudinary. Vui lòng thử lại.");
        }
    }

    // Inspect the file itself; the browser's MIME type and filename are untrusted.
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES)
            throw new DeviceException(HttpStatus.BAD_REQUEST, "Ảnh phải có dung lượng từ 1 byte đến 5 MB");
        // Closing ImageInputStream does not close its underlying upload stream.
        // Both must be closed so Tomcat can remove its temporary file on Windows.
        try (InputStream source = file.getInputStream();
             ImageInputStream stream = ImageIO.createImageInputStream(source)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("Unsupported image");
            ImageReader reader = readers.next();
            try {
                if (!Set.of("JPEG", "PNG", "GIF").contains(reader.getFormatName().toUpperCase(java.util.Locale.ROOT)))
                    throw new IOException("Unsupported format");
                reader.setInput(stream);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 25_000_000)
                    throw new IOException("Too many pixels");
                reader.read(0);
            } finally { reader.dispose(); }
        } catch (IOException | RuntimeException ex) {
            throw new DeviceException(HttpStatus.BAD_REQUEST, "Chỉ nhận ảnh JPG, PNG, GIF hợp lệ, tối đa 25 megapixel");
        }
    }

    // Best effort cleanup must not undo a committed database change.
    public void deleteQuietly(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            if (!configured) throw new IOException("Cloudinary unavailable");
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, Map.of("invalidate", true));
            if (!Set.of("ok", "not found").contains(result.get("result")))
                throw new IOException("Unexpected delete response");
        } catch (IOException | RuntimeException ex) {
            log.warn("Cloudinary cleanup pending for public_id={}", publicId);
        }
    }
}
