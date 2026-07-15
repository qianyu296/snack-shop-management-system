package com.snackadmin.enums;

import lombok.Getter;

/**
 * 角色编码
 */
@Getter
public enum RoleCode {

    ADMIN("ADMIN", "管理员"),
    SHOP_MANAGER("SHOP_MANAGER", "店长");

    private final String code;
    private final String desc;

    RoleCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
