package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.enums.InventoryChangeType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水
 */
@Data
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long materialId;
    private InventoryChangeType changeType;
    private BigDecimal beforeStock;
    private BigDecimal changeQuantity;
    private BigDecimal afterStock;
    private String businessNo;
    private String remark;
    private Long operatorId;
    private LocalDateTime createdAt;
}
