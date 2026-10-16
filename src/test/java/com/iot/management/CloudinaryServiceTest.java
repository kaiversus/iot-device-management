package com.iot.management;

import com.iot.management.exception.DeviceException;
import com.iot.management.service.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.*;

class CloudinaryServiceTest {
    private final CloudinaryService service = new CloudinaryService("", "", "");
    @Test void rejectsFakeAndOversizedImages() {
        assertThatThrownBy(() -> service.upload(new MockMultipartFile("image", "fake.png", "image/png", "not a picture".getBytes())))
            .isInstanceOfSatisfying(DeviceException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.upload(new MockMultipartFile("image", new byte[5 * 1024 * 1024 + 1])))
            .isInstanceOf(DeviceException.class);
    }
    @Test void validImageReportsMissingConfiguration() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        assertThatThrownBy(() -> service.upload(new MockMultipartFile("image", "valid.png", "image/png", output.toByteArray())))
            .isInstanceOfSatisfying(DeviceException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test void releasesUploadedFileOnBothSuccessfulAndFailedValidation() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        for (byte[] data : new byte[][]{output.toByteArray(), new byte[]{1, 2, 3}}) {
            java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean();
            MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png", data) {
                @Override public java.io.InputStream getInputStream() throws java.io.IOException {
                    return new java.io.FilterInputStream(super.getInputStream()) {
                        @Override public void close() throws java.io.IOException {
                            closed.set(true);
                            super.close();
                        }
                    };
                }
            };
            assertThatThrownBy(() -> service.upload(file)).isInstanceOf(DeviceException.class);
            assertThat(closed.get()).as("Upload stream must be closed so Windows can delete it").isTrue();
        }
    }
}
