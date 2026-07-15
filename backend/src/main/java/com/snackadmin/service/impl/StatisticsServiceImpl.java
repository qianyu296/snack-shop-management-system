package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackadmin.entity.*;
import com.snackadmin.enums.*;
import com.snackadmin.mapper.*;
import com.snackadmin.service.StatisticsService;
import com.snackadmin.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final DishCategoryMapper dishCategoryMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final MaterialMapper materialMapper;

    @Override
    public StatisticsVO getStatistics(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDateTime rangeStart = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime rangeEnd = LocalDateTime.of(end, LocalTime.MAX);

        StatisticsVO vo = new StatisticsVO();

        // 查询范围内已完成订单
        List<Order> completedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.COMPLETED)
                .between(Order::getCreatedAt, rangeStart, rangeEnd));

        // 营业额趋势 (按天)
        Map<LocalDate, BigDecimal> revenueMap = new TreeMap<>();
        for (Order o : completedOrders) {
            LocalDate d = o.getCreatedAt().toLocalDate();
            revenueMap.merge(d, o.getTotalAmount(), BigDecimal::add);
        }
        vo.setRevenueTrend(revenueMap.entrySet().stream().map(e -> {
            StatisticsVO.TrendItem item = new StatisticsVO.TrendItem();
            item.setDate(e.getKey().toString()); item.setAmount(e.getValue()); return item;
        }).toList());

        // 订单量趋势
        List<Order> allOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .between(Order::getCreatedAt, rangeStart, rangeEnd));
        Map<LocalDate, long[]> orderCountMap = new TreeMap<>();
        for (Order o : allOrders) {
            orderCountMap.computeIfAbsent(o.getCreatedAt().toLocalDate(), k -> new long[3]);
            long[] arr = orderCountMap.get(o.getCreatedAt().toLocalDate());
            arr[0]++;
            if (o.getStatus() == OrderStatus.COMPLETED) arr[1]++;
            if (o.getStatus() == OrderStatus.CANCELLED) arr[2]++;
        }
        vo.setOrderCountTrend(orderCountMap.entrySet().stream().map(e -> {
            StatisticsVO.OrderCountTrend t = new StatisticsVO.OrderCountTrend();
            t.setDate(e.getKey().toString()); t.setCreated(e.getValue()[0]);
            t.setCompleted(e.getValue()[1]); t.setCancelled(e.getValue()[2]); return t;
        }).toList());

        // 菜品销量排行
        Map<String, Long> dishRankMap = new HashMap<>();
        for (Order o : completedOrders) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            for (OrderItem item : items) {
                dishRankMap.merge(item.getDishName(), (long) item.getQuantity(), Long::sum);
            }
        }
        vo.setDishRank(dishRankMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(10)
                .map(e -> { StatisticsVO.DishRank r = new StatisticsVO.DishRank(); r.setDishName(e.getKey()); r.setCount(e.getValue()); return r; })
                .toList());

        // 分类销量占比
        Map<String, Long> catMap = new HashMap<>();
        for (Order o : completedOrders) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            for (OrderItem item : items) {
                if (item.getCategoryName() != null) catMap.merge(item.getCategoryName(), (long) item.getQuantity(), Long::sum);
            }
        }
        vo.setCategoryPie(catMap.entrySet().stream().map(e -> {
            StatisticsVO.CategoryPie p = new StatisticsVO.CategoryPie(); p.setCategoryName(e.getKey()); p.setValue(e.getValue()); return p;
        }).toList());

        // 订单类型占比
        long dineIn = allOrders.stream().filter(o -> o.getOrderType() == OrderType.DINE_IN).count();
        long takeaway = allOrders.stream().filter(o -> o.getOrderType() == OrderType.TAKEAWAY).count();
        StatisticsVO.OrderTypePie otp = new StatisticsVO.OrderTypePie();
        otp.setDineIn(dineIn); otp.setTakeaway(takeaway);
        vo.setOrderTypePie(otp);

        // 完成率
        long totalNonPending = allOrders.stream().filter(o -> o.getStatus() != OrderStatus.PENDING).count();
        long completedCount = completedOrders.size();
        vo.setCompletionRate(totalNonPending > 0
                ? BigDecimal.valueOf(completedCount).divide(BigDecimal.valueOf(totalNonPending), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);

        // 采购成本趋势
        List<PurchaseOrder> warehoused = purchaseOrderMapper.selectList(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getStatus, PurchaseStatus.WAREHOUSED)
                .between(PurchaseOrder::getCreatedAt, rangeStart, rangeEnd));
        Map<LocalDate, BigDecimal> costMap = new TreeMap<>();
        for (PurchaseOrder po : warehoused) {
            costMap.merge(po.getCreatedAt().toLocalDate(), po.getTotalAmount(), BigDecimal::add);
        }
        vo.setCostTrend(costMap.entrySet().stream().map(e -> {
            StatisticsVO.CostTrend ct = new StatisticsVO.CostTrend(); ct.setDate(e.getKey().toString()); ct.setAmount(e.getValue()); return ct;
        }).toList());

        // 原料消耗排行
        List<InventoryRecord> consumeRecords = inventoryRecordMapper.selectList(new LambdaQueryWrapper<InventoryRecord>()
                .eq(InventoryRecord::getChangeType, InventoryChangeType.ORDER_CONSUME)
                .between(InventoryRecord::getCreatedAt, rangeStart, rangeEnd));
        Map<String, BigDecimal> consumeMap = new HashMap<>();
        for (InventoryRecord r : consumeRecords) {
            Material m = materialMapper.selectById(r.getMaterialId());
            String name = m != null ? m.getName() : "未知";
            consumeMap.merge(name, r.getChangeQuantity().negate(), BigDecimal::add);
        }
        vo.setMaterialConsume(consumeMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()).limit(10)
                .map(e -> { StatisticsVO.MaterialConsume mc = new StatisticsVO.MaterialConsume(); mc.setMaterialName(e.getKey()); mc.setTotalQuantity(e.getValue()); return mc; })
                .toList());

        return vo;
    }
}
