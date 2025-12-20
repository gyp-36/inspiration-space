package com.is.inspirationspacecommon.config;

import io.minio.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StorageService {
    private final MinioClient minioClient;
    private final MinioConfig minioConfig; // 从配置文件读取

    @Autowired
    public StorageService(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    // ====== 1. 上传文件 ======
    // 修复后的 upload 方法
    @SneakyThrows
    public String upload(MultipartFile file, String bucketName, String objectKey) {
        // 1. 检查桶是否存在，不存在则创建
        if (!bucketExists(bucketName)) {
            createBucket(bucketName);
        }

        // 2. 使用 putObject 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("上传文件到MinIO失败: bucketName={}, objectKey={}, fileSize={}", 
                     bucketName, objectKey, file.getSize(), e);
            throw e;
        }

        return objectKey;
    }


    // ====== 2. 生成预签名URL ======
    @SneakyThrows
    public String getPreSignedUrl(String bucketName, String objectKey, int expiry, TimeUnit unit) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(io.minio.http.Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expiry, unit)
                    .build()
            );
        } catch (Exception e) {
            log.error("生成预签名URL失败: bucketName={}, objectKey={}", bucketName, objectKey, e);
            throw e;
        }
    }

    // ====== 3. 删除文件 ======
    @SneakyThrows
    public void delete(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build()
            );
        } catch (Exception e) {
            log.error("删除MinIO文件失败: bucketName={}, objectKey={}", bucketName, objectKey, e);
            throw e;
        }
    }

    // ====== 4. 检查桶是否存在 ======
    private boolean bucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            return exists;
        } catch (Exception e) {
            log.error("检查MinIO桶是否存在失败: bucketName={}", bucketName, e);
            return false;
        }
    }

    // ====== 5. 创建桶 ======
    @SneakyThrows
    private void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("成功创建MinIO桶: {}", bucketName);
        } catch (Exception e) {
            log.error("创建MinIO桶失败: bucketName={}", bucketName, e);
            throw e;
        }
    }
}