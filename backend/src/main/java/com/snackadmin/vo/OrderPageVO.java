package com.snackadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单分页列表VO
 */
@Data
public class OrderPageVO {

    private Long id;
    private String orderNo;
    private String orderType;
    private String tableNo;
    private Integer pickupNo;
    private BigDecimal totalAmount;
    private String status;
    private String createdByName;
    private LocalDateTime createdAt;
}
