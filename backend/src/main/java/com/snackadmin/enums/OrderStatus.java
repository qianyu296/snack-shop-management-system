package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单状态
 */
@Getter
public enum OrderStatus {

    PENDING("PENDING", "待确认"),
    COOKING("COOKING", "制作中"),
    READY("READY", "待取餐"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
