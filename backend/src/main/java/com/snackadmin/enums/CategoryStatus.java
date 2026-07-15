package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 分类启用状态
 */
@Getter
public enum CategoryStatus {

    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "停用");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    CategoryStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
