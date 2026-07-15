package com.snackadmin.util;

import com.snackadmin.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUploadUtil {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传菜品图片
     */
    public String uploadDishImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型，仅支持 JPG、PNG、WebP");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 5MB");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "dish/" + UUID.randomUUID().toString().replace("-", "") + extension;

            Path uploadDir = Paths.get(uploadPath).resolve("dish");
            Files.createDirectories(uploadDir);
            Path targetPath = Paths.get(uploadPath).resolve(filename);
            file.transferTo(targetPath.toFile());

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }
        try {
            // fileUrl 格式: /uploads/dish/xxx.jpg
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadPath).resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 删除失败不抛出异常
        }
    }
}
