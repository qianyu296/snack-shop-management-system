package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String avatar;
    private UserStatus status;
    private String rejectReason;
}
