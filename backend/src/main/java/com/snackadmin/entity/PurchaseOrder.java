package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.PurchaseStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {

    private String purchaseNo;
    private Long supplierId;
    private LocalDate purchaseDate;
    private BigDecimal totalAmount;
    private PurchaseStatus status;
    private String cancelReason;
    private String remark;
}
