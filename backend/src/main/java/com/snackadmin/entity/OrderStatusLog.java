package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志
 */
@Data
@TableName("order_status_log")
public class OrderStatusLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String beforeStatus;
    private String afterStatus;
    private String remark;
    private Long operatorId;
    private LocalDateTime createdAt;
}
