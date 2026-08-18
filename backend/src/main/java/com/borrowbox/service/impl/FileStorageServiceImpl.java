package com.borrowbox.service.impl;

import com.borrowbox.exception.BadRequestException;
import com.borrowbox.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads/items}")
    private String uploadDir;

    private Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Initialized upload directory at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create upload directory", ex);
            throw new RuntimeException("Could not initialize upload storage location", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException("Invalid file type: '" + extension + "'. Allowed image types: JPG, JPEG, PNG, WEBP, GIF.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid MIME content type: '" + contentType + "'. Only image files are allowed.");
        }

        String uniqueFileName = UUID.randomUUID() + "." + extension.toLowerCase();

        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Stored file: {}", uniqueFileName);
            return "/uploads/" + uniqueFileName;
        } catch (IOException ex) {
            log.error("Failed to store file: " + uniqueFileName, ex);
            throw new RuntimeException("Failed to store file on disk", ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            String filename = fileUrl.replace("/uploads/", "");
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filename);
        } catch (IOException ex) {
            log.warn("Could not delete file: {}", fileUrl);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
