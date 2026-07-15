package com.snackadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜品分类保存请求
 */
@Data
public class DishCategorySaveDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 20, message = "分类名称长度为2-20个字符")
    private String name;

    @NotNull(message = "排序值不能为空")
    private Integer sort;

    @NotBlank(message = "状态不能为空")
    private String status;

    @Size(max = 200, message = "备注最长200个字符")
    private String remark;
}
