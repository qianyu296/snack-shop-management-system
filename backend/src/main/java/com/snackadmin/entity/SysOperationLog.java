package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestParams;
    private Long operatorId;
    private String operatorName;
    private String operatorIp;
    private String result;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime createdAt;
}
