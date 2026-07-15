package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存流水查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryRecordQueryDTO extends PageQuery {

    private Long materialId;
    private String changeType;
    private String businessNo;
    private String startDate;
    private String endDate;
}
