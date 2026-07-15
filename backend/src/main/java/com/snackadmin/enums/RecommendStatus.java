package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 菜品推荐状态
 */
@Getter
public enum RecommendStatus {

    NORMAL("NORMAL", "普通"),
    RECOMMENDED("RECOMMENDED", "推荐");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    RecommendStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
