package com.snackadmin.dto;

import com.snackadmin.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜品分类查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DishCategoryQueryDTO extends PageQuery {

    private String name;
    private String status;
}
