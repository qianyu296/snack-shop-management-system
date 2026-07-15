package com.snackadmin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户分页列表VO
 */
@Data
public class UserPageVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
}
