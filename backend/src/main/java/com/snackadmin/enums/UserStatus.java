package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户账号状态
 */
@Getter
public enum UserStatus {

    PENDING("PENDING", "待审核"),
    ENABLED("ENABLED", "已启用"),
    DISABLED("DISABLED", "已禁用"),
    REJECTED("REJECTED", "已驳回");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    UserStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
