package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 菜品销售状态
 */
@Getter
public enum DishSaleStatus {

    DRAFT("DRAFT", "草稿"),
    ON_SALE("ON_SALE", "已上架"),
    OFF_SALE("OFF_SALE", "已下架");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    DishSaleStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
