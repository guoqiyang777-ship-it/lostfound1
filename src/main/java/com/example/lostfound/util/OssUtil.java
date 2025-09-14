package com.example.lostfound.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Slf4j
@Component
public class OssUtil {

    @Autowired
    private OSS ossClient;

    @Value("${app.oss.bucket-name}")
    private String bucketName;

    @Value("${app.oss.endpoint}")
    private String endpoint;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param dir  目录
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String dir) {
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            String objectName = dir + "/" + fileName;

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file.getInputStream());

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 返回文件URL
            // 检查endpoint是否已包含https://前缀
            String endpointUrl = endpoint;
            if (endpoint.startsWith("https://")) {
                endpointUrl = endpoint.substring(8); // 移除https://前缀
            } else if (endpoint.startsWith("http://")) {
                endpointUrl = endpoint.substring(7); // 移除http://前缀
            }
            return "https://" + bucketName + "." + endpointUrl + "/" + objectName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传头像
     *
     * @param file 文件
     * @return 文件URL
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatar");
    }

    /**
     * 上传物品图片
     *
     * @param file 文件
     * @return 文件URL
     */
    public String uploadItemImage(MultipartFile file) {
        return uploadFile(file, "item");
    }
}