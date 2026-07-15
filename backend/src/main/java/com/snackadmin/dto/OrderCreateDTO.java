package com.snackadmin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单创建/编辑请求
 */
@Data
public class OrderCreateDTO {

    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    private String tableNo;

    @Size(max = 200, message = "顾客备注最长200个字符")
    private String customerRemark;

    @Size(max = 200, message = "内部备注最长200个字符")
    private String internalRemark;

    @NotEmpty(message = "至少选择一个菜品")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "菜品ID不能为空")
        private Long dishId;

        private Long specId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
