package com.example.lostfound.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 失物招领信息DTO
 */
@Data
public class ItemDTO {
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
    
    /**
     * 描述
     */
    @NotBlank(message = "描述不能为空")
    private String description;
    
    /**
     * 类型（lost/claim）
     */
    @NotBlank(message = "类型不能为空")
    private String type;
    
    /**
     * 地点
     */
    @NotBlank(message = "地点不能为空")
    private String location;
    
    /**
     * 时间（yyyy-MM-dd HH:mm:ss）
     */
    @NotBlank(message = "时间不能为空")
    private String itemTime;
    
    /**
     * 图片URL
     */
    private String imageUrl;
}