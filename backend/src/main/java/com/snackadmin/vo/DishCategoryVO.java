package com.snackadmin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜品分类详情VO
 */
@Data
public class DishCategoryVO {

    private Long id;
    private String name;
    private Integer sort;
    private String status;
    private String remark;
    private Long dishCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
