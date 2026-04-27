package com.track.service;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.TrackFileAsset;
import com.track.repository.TrackFileAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileAssetService {
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList("jpg", "png"));

    @Autowired
    private TrackFileAssetRepository trackFileAssetRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    public Result<Map<String, Object>> uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择图片文件");
        }

        long fileSize = file.getSize();
        if (fileSize <= 0) {
            return Result.error("文件大小无效");
        }
        if (fileSize > MAX_IMAGE_SIZE) {
            return Result.error("图片大小不能超过2MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = normalizeExtension(extractExtension(originalName));
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            return Result.error("仅支持jpg/png格式");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            return Result.error("读取上传文件失败");
        }
        if (fileBytes.length == 0) {
            return Result.error("上传文件为空");
        }

        String detectedExt = detectImageExtension(fileBytes);
        if (detectedExt == null || !ALLOWED_EXT.contains(detectedExt)) {
            return Result.error("文件内容不是有效的jpg/png图片");
        }
        if (!Objects.equals(ext, detectedExt)) {
            return Result.error("文件后缀与文件内容不一致");
        }

        TrackFileAsset asset = new TrackFileAsset();
        asset.setFileId(UUID.randomUUID().toString().replace("-", ""));
        asset.setOriginalName(normalizeOriginalName(originalName, detectedExt));
        asset.setFileExt(detectedExt);
        asset.setContentType(toContentType(detectedExt));
        asset.setFileSize((long) fileBytes.length);
        asset.setFileData(fileBytes);
        asset.setCreateBy(permissionChecker.getCurrentUserId());
        asset.setCreateTime(LocalDateTime.now());
        trackFileAssetRepository.save(asset);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileId", asset.getFileId());
        data.put("fileName", asset.getOriginalName());
        data.put("fileSize", asset.getFileSize());
        data.put("contentType", asset.getContentType());
        return Result.success(data);
    }

    public ResponseEntity<byte[]> preview(String fileId) {
        String normalizedFileId = trim(fileId);
        if (normalizedFileId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        TrackFileAsset asset = trackFileAssetRepository.findByFileId(normalizedFileId);
        if (asset == null || asset.getFileData() == null || asset.getFileData().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(asset.getContentType()));
        headers.setContentLength(asset.getFileSize() == null ? asset.getFileData().length : asset.getFileSize());
        headers.set("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        headers.set("Pragma", "no-cache");
        String safeName = asset.getOriginalName() == null ? normalizedFileId : asset.getOriginalName().replace("\"", "");
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"");

        return new ResponseEntity<>(asset.getFileData(), headers, HttpStatus.OK);
    }

    private String detectImageExtension(byte[] fileBytes) {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(fileBytes))) {
            if (imageInputStream == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            String format = reader.getFormatName();
            return normalizeExtension(format);
        } catch (Exception e) {
            return null;
        }
    }

    private String toContentType(String ext) {
        if ("png".equals(ext)) {
            return "image/png";
        }
        return "image/jpeg";
    }

    private String extractExtension(String fileName) {
        String normalized = trim(fileName);
        if (normalized == null) {
            return null;
        }
        int index = normalized.lastIndexOf('.');
        if (index < 0 || index == normalized.length() - 1) {
            return null;
        }
        return normalized.substring(index + 1);
    }

    private String normalizeExtension(String ext) {
        String value = trim(ext);
        if (value == null) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if ("jpeg".equals(lower)) {
            return "jpg";
        }
        if ("jpg".equals(lower) || "png".equals(lower)) {
            return lower;
        }
        return null;
    }

    private String normalizeOriginalName(String originalName, String ext) {
        String value = trim(originalName);
        if (value == null) {
            return "image." + ext;
        }
        return value;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
