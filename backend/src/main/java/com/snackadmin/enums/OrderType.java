package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单类型
 */
@Getter
public enum OrderType {

    DINE_IN("DINE_IN", "堂食"),
    TAKEAWAY("TAKEAWAY", "打包");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    OrderType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
