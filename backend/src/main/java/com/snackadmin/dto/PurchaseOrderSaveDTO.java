package com.snackadmin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderSaveDTO {
    @NotNull private Long supplierId;
    @NotNull private LocalDate purchaseDate;
    @NotEmpty @Valid private List<Item> items;
    @Size(max = 200) private String remark;

    @Data
    public static class Item {
        @NotNull private Long materialId;
        @NotNull @Positive private BigDecimal quantity;
        @NotNull @PositiveOrZero private BigDecimal unitPrice;
    }
}
