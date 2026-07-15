package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册申请请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度为2-50个字符")
    private String username;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最长50个字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
