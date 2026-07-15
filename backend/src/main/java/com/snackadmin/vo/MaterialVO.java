package com.snackadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 原料详情VO
 */
@Data
public class MaterialVO {

    private Long id;
    private String name;
    private String category;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal safeStock;
    private String status;
    private String remark;
    private Boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
