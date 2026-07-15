package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品配方 (菜品-原料关联)
 */
@Data
@TableName("dish_material")
public class DishMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dishId;
    private Long materialId;
    private BigDecimal quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
