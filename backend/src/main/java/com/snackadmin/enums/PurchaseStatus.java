package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 采购单状态
 */
@Getter
public enum PurchaseStatus {

    DRAFT("DRAFT", "草稿"),
    WAREHOUSED("WAREHOUSED", "已入库"),
    CANCELLED("CANCELLED", "已作废");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    PurchaseStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
