package com.snackadmin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品规格
 */
@Data
@TableName("dish_spec")
public class DishSpec {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dishId;
    private String name;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
