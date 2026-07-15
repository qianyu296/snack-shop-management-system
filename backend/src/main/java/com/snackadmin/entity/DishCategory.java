package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.CategoryStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜品分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish_category")
public class DishCategory extends BaseEntity {

    private String name;
    private Integer sort;
    private CategoryStatus status;
    private String remark;
}
