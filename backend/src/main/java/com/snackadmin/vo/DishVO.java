package com.snackadmin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品详情VO
 */
@Data
public class DishVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal basePrice;
    private String imageUrl;
    private String taste;
    private String description;
    private String recommendStatus;
    private String saleStatus;
    private List<SpecVO> specs;
    private List<MaterialRefVO> materials;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class SpecVO {
        private Long id;
        private String name;
        private BigDecimal price;
    }

    @Data
    public static class MaterialRefVO {
        private Long id;
        private Long materialId;
        private String materialName;
        private String materialUnit;
        private BigDecimal quantity;
    }
}
