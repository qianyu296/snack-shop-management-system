package com.snackadmin.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {
    private TodaySummary today;
    private BaseStats baseStats;
    private List<TrendItem> orderTrend;
    private List<HotDish> hotDishes;
    private List<LowStockMaterial> lowStockMaterials;
    private TodoCount todo;

    @Data public static class TodaySummary {
        private Long completedOrderCount; private BigDecimal todayRevenue;
        private Long pendingOrderCount; private Long cookingOrderCount;
    }
    @Data public static class BaseStats {
        private Long onSaleDishCount; private Long materialCount;
        private Long lowStockMaterialCount; private Long enabledSupplierCount;
    }
    @Data public static class TrendItem {
        private String date; private Long orderCount; private BigDecimal revenue;
    }
    @Data public static class HotDish {
        private String dishName; private Long saleCount;
    }
    @Data public static class LowStockMaterial {
        private Long id; private String name; private String unit;
        private BigDecimal currentStock; private BigDecimal safeStock;
    }
    @Data public static class TodoCount {
        private Long pendingAuditCount; private Long pendingOrderCount; private Long lowStockCount;
    }
}
