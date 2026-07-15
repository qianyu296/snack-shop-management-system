package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 原料查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialQueryDTO extends PageQuery {

    private String name;
    private String category;
    private String status;
    private Boolean lowStock; // 低库存预警筛选
}
