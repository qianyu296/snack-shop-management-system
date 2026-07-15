package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜品查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DishQueryDTO extends PageQuery {

    private String name;
    private Long categoryId;
    private String saleStatus;
    private String recommendStatus;
}
