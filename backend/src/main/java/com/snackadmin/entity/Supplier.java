package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.SupplierStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {

    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private String supplyMaterials;
    private SupplierStatus status;
    private String remark;
}
