package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 订单状态变更请求
 */
@Data
public class OrderStatusChangeDTO {

    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    @Size(max = 500, message = "备注最长500个字符")
    private String remark;
}
