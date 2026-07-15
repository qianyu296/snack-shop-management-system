package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 原料保存请求
 */
@Data
public class MaterialSaveDTO {

    @NotBlank(message = "原料名称不能为空")
    @Size(min = 2, max = 50, message = "原料名称长度为2-50个字符")
    private String name;

    @NotBlank(message = "原料分类不能为空")
    private String category;

    @NotBlank(message = "计量单位不能为空")
    @Size(max = 20, message = "计量单位最长20个字符")
    private String unit;

    @NotNull(message = "安全库存不能为空")
    @PositiveOrZero(message = "安全库存不能为负数")
    private BigDecimal safeStock;

    @NotBlank(message = "状态不能为空")
    private String status;

    @Size(max = 200, message = "备注最长200个字符")
    private String remark;
}
