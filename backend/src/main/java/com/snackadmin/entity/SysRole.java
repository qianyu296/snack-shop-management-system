package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.enums.RoleCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;
    private RoleCode roleCode;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;
}
