package com.snackadmin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
}
