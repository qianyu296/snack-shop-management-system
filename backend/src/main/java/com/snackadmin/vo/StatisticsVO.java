package com.snackadmin.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsVO {
    private List<TrendItem> revenueTrend;
    private List<OrderCountTrend> orderCountTrend;
    private List<DishRank> dishRank;
    private List<CategoryPie> categoryPie;
    private OrderTypePie orderTypePie;
    private BigDecimal completionRate;
    private List<CostTrend> costTrend;
    private List<MaterialConsume> materialConsume;

    @Data public static class TrendItem { private String date; private BigDecimal amount; }
    @Data public static class OrderCountTrend { private String date; private Long created; private Long completed; private Long cancelled; }
    @Data public static class DishRank { private String dishName; private Long count; }
    @Data public static class CategoryPie { private String categoryName; private Long value; }
    @Data public static class OrderTypePie { private Long dineIn; private Long takeaway; }
    @Data public static class CostTrend { private String date; private BigDecimal amount; }
    @Data public static class MaterialConsume { private String materialName; private BigDecimal totalQuantity; }
}
