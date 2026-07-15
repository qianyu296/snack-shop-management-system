package com.snackadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水VO
 */
@Data
public class InventoryRecordVO {

    private Long id;
    private Long materialId;
    private String materialName;
    private String materialUnit;
    private String changeType;
    private BigDecimal beforeStock;
    private BigDecimal changeQuantity;
    private BigDecimal afterStock;
    private String businessNo;
    private String remark;
    private String operatorName;
    private LocalDateTime createdAt;
}
