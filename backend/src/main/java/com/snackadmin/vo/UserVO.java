package com.snackadmin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情VO
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String avatar;
    private String status;
    private String rejectReason;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
