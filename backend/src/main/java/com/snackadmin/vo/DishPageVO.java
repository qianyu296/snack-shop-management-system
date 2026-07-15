package com.snackadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品分页列表VO
 */
@Data
public class DishPageVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal basePrice;
    private String imageUrl;
    private String taste;
    private String recommendStatus;
    private String saleStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
