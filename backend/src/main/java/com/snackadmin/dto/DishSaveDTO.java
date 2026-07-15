package com.snackadmin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品保存请求
 */
@Data
public class DishSaveDTO {

    @NotNull(message = "菜品分类不能为空")
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    @Size(min = 2, max = 50, message = "菜品名称长度为2-50个字符")
    private String name;

    @NotNull(message = "基础价格不能为空")
    @PositiveOrZero(message = "价格不能为负数")
    private BigDecimal basePrice;

    private String imageUrl;

    @Size(max = 50, message = "口味最长50个字符")
    private String taste;

    @Size(max = 500, message = "描述最长500个字符")
    private String description;

    @NotBlank(message = "推荐状态不能为空")
    private String recommendStatus;

    @NotBlank(message = "销售状态不能为空")
    private String saleStatus;

    /** 规格列表 */
    @Valid
    private List<DishSpecDTO> specs;

    /** 配方列表 */
    @Valid
    private List<DishMaterialDTO> materials;

    @Data
    public static class DishSpecDTO {
        @NotBlank(message = "规格名称不能为空")
        @Size(max = 30, message = "规格名称最长30个字符")
        private String name;

        @NotNull(message = "规格价格不能为空")
        @PositiveOrZero(message = "价格不能为负数")
        private BigDecimal price;
    }

    @Data
    public static class DishMaterialDTO {
        @NotNull(message = "原料ID不能为空")
        private Long materialId;

        @NotNull(message = "单份用量不能为空")
        @Positive(message = "单份用量必须大于0")
        private BigDecimal quantity;
    }
}
