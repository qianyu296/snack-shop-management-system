package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存调整请求
 */
@Data
public class StockAdjustDTO {

    @NotNull(message = "原料ID不能为空")
    private Long materialId;

    @NotBlank(message = "调整类型不能为空")
    private String changeType; // SURPLUS_ADJUST / LOSS_ADJUST

    @NotNull(message = "调整数量不能为空")
    @Positive(message = "调整数量必须大于0")
    private BigDecimal quantity;

    @NotBlank(message = "调整原因不能为空")
    private String remark;

    @NotBlank(message = "业务单号不能为空")
    private String businessNo; // 用于关联调整记录
}
