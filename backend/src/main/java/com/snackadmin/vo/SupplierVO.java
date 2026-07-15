package com.snackadmin.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SupplierVO {
    private Long id;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private String supplyMaterials;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
