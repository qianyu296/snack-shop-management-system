package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends PageQuery {

    private String orderNo;
    private Integer pickupNo;
    private String orderType;
    private String status;
    private Long createdBy;
    private String startDate;
    private String endDate;
}
