package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 原料状态
 */
@Getter
public enum MaterialStatus {

    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "停用");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    MaterialStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
