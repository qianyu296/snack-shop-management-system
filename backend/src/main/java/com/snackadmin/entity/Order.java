package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.OrderStatus;
import com.snackadmin.enums.OrderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private String orderNo;
    private OrderType orderType;
    private String tableNo;
    private Integer pickupNo;
    private BigDecimal totalAmount;
    private String customerRemark;
    private String internalRemark;
    private String cancelReason;
    private OrderStatus status;
}
