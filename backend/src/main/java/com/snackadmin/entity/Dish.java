package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.snackadmin.common.BaseEntity;
import com.snackadmin.enums.DishSaleStatus;
import com.snackadmin.enums.RecommendStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 菜品
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish")
public class Dish extends BaseEntity {

    private Long categoryId;
    private String name;
    private BigDecimal basePrice;
    private String imageUrl;
    private String taste;
    private String description;
    private RecommendStatus recommendStatus;
    private DishSaleStatus saleStatus;
}
