package com.snackadmin.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "页码从1开始")
    private int pageNum = 1;

    @Min(value = 1, message = "每页至少1条")
    @Max(value = 100, message = "每页最多100条")
    private int pageSize = 10;
}
