package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账号审核请求
 */
@Data
public class AuditRequest {

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @NotBlank(message = "驳回原因不能为空")
    private String rejectReason;
}
