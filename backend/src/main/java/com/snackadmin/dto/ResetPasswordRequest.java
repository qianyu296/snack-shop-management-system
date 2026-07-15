package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员重置密码请求
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String newPassword;
}
