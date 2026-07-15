package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 原料分类
 */
@Getter
public enum MaterialCategory {

    STAPLE("STAPLE", "主食"),
    VEGETABLE("VEGETABLE", "蔬菜"),
    MEAT("MEAT", "肉类"),
    SEASONING("SEASONING", "调料"),
    PACKAGING("PACKAGING", "包装材料"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    MaterialCategory(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
