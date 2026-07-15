package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierQueryDTO extends PageQuery {
    private String name;
    private String contactName;
    private String contactPhone;
    private String status;
}
