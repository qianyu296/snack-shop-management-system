package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderQueryDTO extends PageQuery {
    private String purchaseNo;
    private Long supplierId;
    private String status;
    private String startDate;
    private String endDate;
}
