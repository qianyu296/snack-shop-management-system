package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.MaterialCategory;
import com.snackadmin.enums.MaterialStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 原料
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material")
public class Material extends BaseEntity {

    private String name;
    private MaterialCategory category;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal safeStock;
    private MaterialStatus status;
    private String remark;
}
