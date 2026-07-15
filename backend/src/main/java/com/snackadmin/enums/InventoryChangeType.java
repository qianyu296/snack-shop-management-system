package com.snackadmin.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 库存变动类型
 */
@Getter
public enum InventoryChangeType {

    PURCHASE_IN("PURCHASE_IN", "采购入库"),
    ORDER_CONSUME("ORDER_CONSUME", "订单消耗"),
    SURPLUS_ADJUST("SURPLUS_ADJUST", "盘盈调整"),
    LOSS_ADJUST("LOSS_ADJUST", "盘亏调整");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    InventoryChangeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
