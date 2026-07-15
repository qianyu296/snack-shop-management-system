package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackadmin.entity.*;
import com.snackadmin.enums.*;
import com.snackadmin.mapper.*;
import com.snackadmin.service.DashboardService;
import com.snackadmin.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderMapper orderMapper;
    private final DishMapper dishMapper;
    private final MaterialMapper materialMapper;
    private final SupplierMapper supplierMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public DashboardVO getDashboard() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        DashboardVO vo = new DashboardVO();

        // 今日概览
        var todayWrapper = new LambdaQueryWrapper<Order>();
        var completedWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.COMPLETED)
                .between(Order::getCreatedAt, todayStart, todayEnd);
        var pendingWrapper = new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatus.PENDING);
        var cookingWrapper = new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatus.COOKING);

        DashboardVO.TodaySummary today = new DashboardVO.TodaySummary();
        today.setCompletedOrderCount(orderMapper.selectCount(completedWrapper));
        List<Order> completedToday = orderMapper.selectList(completedWrapper);
        today.setTodayRevenue(completedToday.stream().map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        today.setPendingOrderCount(orderMapper.selectCount(pendingWrapper));
        today.setCookingOrderCount(orderMapper.selectCount(cookingWrapper));
        vo.setToday(today);

        // 基础数据
        DashboardVO.BaseStats base = new DashboardVO.BaseStats();
        base.setOnSaleDishCount(dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getSaleStatus, DishSaleStatus.ON_SALE)));
        base.setMaterialCount(materialMapper.selectCount(null));
        // 使用变量避免类型推断问题
        var lowStockWrapper = new LambdaQueryWrapper<Material>();
        lowStockWrapper.apply("current_stock <= safe_stock");
        base.setLowStockMaterialCount(materialMapper.selectCount(lowStockWrapper));
        base.setEnabledSupplierCount(supplierMapper.selectCount(
                new LambdaQueryWrapper<Supplier>().eq(Supplier::getStatus, SupplierStatus.ENABLED)));
        vo.setBaseStats(base);

        // 订单趋势（最近7天）
        List<DashboardVO.TrendItem> trend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now().minusDays(6 - i);
            LocalDateTime ds = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime de = LocalDateTime.of(d, LocalTime.MAX);
            var dayCompletedWrapper = new LambdaQueryWrapper<Order>()
                    .eq(Order::getStatus, OrderStatus.COMPLETED)
                    .between(Order::getCreatedAt, ds, de);
            DashboardVO.TrendItem item = new DashboardVO.TrendItem();
            item.setDate(d.toString());
            item.setOrderCount(orderMapper.selectCount(dayCompletedWrapper));
            List<Order> dayOrders = orderMapper.selectList(dayCompletedWrapper);
            item.setRevenue(dayOrders.stream().map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            trend.add(item);
        }
        vo.setOrderTrend(trend);

        // 热销排行（略）
        vo.setHotDishes(new ArrayList<>());

        // 库存预警
        var lowStockMatsWrapper = new LambdaQueryWrapper<Material>();
        lowStockMatsWrapper.apply("current_stock <= safe_stock");
        lowStockMatsWrapper.eq(Material::getStatus, MaterialStatus.ENABLED);
        List<Material> lowStockMats = materialMapper.selectList(lowStockMatsWrapper);
        vo.setLowStockMaterials(lowStockMats.stream().map(m -> {
            DashboardVO.LowStockMaterial lm = new DashboardVO.LowStockMaterial();
            lm.setId(m.getId()); lm.setName(m.getName()); lm.setUnit(m.getUnit());
            lm.setCurrentStock(m.getCurrentStock()); lm.setSafeStock(m.getSafeStock());
            return lm;
        }).toList());

        // 待办事项
        DashboardVO.TodoCount todo = new DashboardVO.TodoCount();
        todo.setPendingAuditCount(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, UserStatus.PENDING)));
        todo.setPendingOrderCount(today.getPendingOrderCount());
        todo.setLowStockCount((long) lowStockMats.size());
        vo.setTodo(todo);

        return vo;
    }
}
