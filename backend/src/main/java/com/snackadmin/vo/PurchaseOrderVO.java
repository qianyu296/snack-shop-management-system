package com.snackadmin.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String purchaseNo;
    private Long supplierId;
    private String supplierName;
    private LocalDate purchaseDate;
    private BigDecimal totalAmount;
    private String status;
    private String cancelReason;
    private String remark;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Item> items;

    @Data
    public static class Item {
        private Long id;
        private Long materialId;
        private String materialName;
        private String materialUnit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}
