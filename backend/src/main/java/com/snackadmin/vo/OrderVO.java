package com.snackadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private String orderType;
    private String tableNo;
    private Integer pickupNo;
    private BigDecimal totalAmount;
    private String customerRemark;
    private String internalRemark;
    private String cancelReason;
    private String status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemVO> items;
    private List<OrderStatusLogVO> statusLogs;

    @Data
    public static class OrderItemVO {
        private Long id;
        private Long dishId;
        private String dishName;
        private String categoryName;
        private String specName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal amount;
    }

    @Data
    public static class OrderStatusLogVO {
        private Long id;
        private String beforeStatus;
        private String afterStatus;
        private String remark;
        private String operatorName;
        private LocalDateTime createdAt;
    }
}
