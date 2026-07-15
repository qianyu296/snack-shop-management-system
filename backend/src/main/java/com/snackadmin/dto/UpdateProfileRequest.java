package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人信息请求
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50个字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
