package com.snackadmin.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.dto.OrderCreateDTO;
import com.snackadmin.dto.OrderQueryDTO;
import com.snackadmin.entity.*;
import com.snackadmin.enums.*;
import com.snackadmin.mapper.*;
import com.snackadmin.service.OrderService;
import com.snackadmin.vo.OrderPageVO;
import com.snackadmin.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final DishMapper dishMapper;
    private final DishSpecMapper dishSpecMapper;
    private final DishCategoryMapper dishCategoryMapper;
    private final MaterialMapper materialMapper;
    private final DishMaterialMapper dishMaterialMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public OrderVO create(OrderCreateDTO dto, Long operatorId) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setOrderType(OrderType.valueOf(dto.getOrderType()));

        // 堂食时桌号必填
        if (order.getOrderType() == OrderType.DINE_IN) {
            if (!StringUtils.hasText(dto.getTableNo())) {
                throw new BusinessException("堂食订单请填写桌号");
            }
            order.setTableNo(dto.getTableNo());
        }

        order.setPickupNo(generatePickupNo());
        order.setCustomerRemark(dto.getCustomerRemark());
        order.setInternalRemark(dto.getInternalRemark());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedBy(operatorId);
        order.setUpdatedBy(operatorId);

        // 计算金额并构建明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            if (dish == null || dish.getSaleStatus() != DishSaleStatus.ON_SALE) {
                throw new BusinessException("菜品 [" + (dish != null ? dish.getName() : itemDTO.getDishId()) + "] 不可售");
            }

            DishCategory category = dishCategoryMapper.selectById(dish.getCategoryId());

            BigDecimal unitPrice = dish.getBasePrice();
            String specName = null;

            // 规格价格
            if (itemDTO.getSpecId() != null) {
                DishSpec spec = dishSpecMapper.selectById(itemDTO.getSpecId());
                if (spec == null || !spec.getDishId().equals(dish.getId())) {
                    throw new BusinessException("菜品规格不存在");
                }
                unitPrice = spec.getPrice();
                specName = spec.getName();
            }

            int quantity = itemDTO.getQuantity();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(amount);

            OrderItem item = new OrderItem();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setCategoryName(category != null ? category.getName() : null);
            item.setSpecName(specName);
            item.setUnitPrice(unitPrice);
            item.setQuantity(quantity);
            item.setAmount(amount);
            items.add(item);
        }

        order.setTotalAmount(totalAmount);
        save(order);

        // 保存明细
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 记录状态日志
        saveStatusLog(order.getId(), null, OrderStatus.PENDING.getCode(), "创建订单", operatorId);

        return getDetail(order.getId());
    }

    @Override
    @Transactional
    public OrderVO update(Long id, OrderCreateDTO dto, Long operatorId) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("仅待确认订单允许修改");
        }

        order.setOrderType(OrderType.valueOf(dto.getOrderType()));
        if (order.getOrderType() == OrderType.DINE_IN) {
            if (!StringUtils.hasText(dto.getTableNo())) {
                throw new BusinessException("堂食订单请填写桌号");
            }
            order.setTableNo(dto.getTableNo());
        } else {
            order.setTableNo(null);
        }
        order.setCustomerRemark(dto.getCustomerRemark());
        order.setInternalRemark(dto.getInternalRemark());
        order.setUpdatedBy(operatorId);

        // 删除旧明细，重建
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Dish dish = dishMapper.selectById(itemDTO.getDishId());
            if (dish == null || dish.getSaleStatus() != DishSaleStatus.ON_SALE) {
                throw new BusinessException("菜品 [" + (dish != null ? dish.getName() : "") + "] 不可售");
            }

            BigDecimal unitPrice = dish.getBasePrice();
            String specName = null;
            if (itemDTO.getSpecId() != null) {
                DishSpec spec = dishSpecMapper.selectById(itemDTO.getSpecId());
                if (spec == null || !spec.getDishId().equals(dish.getId())) {
                    throw new BusinessException("菜品规格不存在");
                }
                unitPrice = spec.getPrice();
                specName = spec.getName();
            }

            int quantity = itemDTO.getQuantity();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(amount);

            OrderItem item = new OrderItem();
            item.setOrderId(id);
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            DishCategory category = dishCategoryMapper.selectById(dish.getCategoryId());
            item.setCategoryName(category != null ? category.getName() : null);
            item.setSpecName(specName);
            item.setUnitPrice(unitPrice);
            item.setQuantity(quantity);
            item.setAmount(amount);
            orderItemMapper.insert(item);
        }

        order.setTotalAmount(totalAmount);
        updateById(order);

        return getDetail(id);
    }

    @Override
    public OrderVO getDetail(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType(order.getOrderType().getCode());
        vo.setTableNo(order.getTableNo());
        vo.setPickupNo(order.getPickupNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setCustomerRemark(order.getCustomerRemark());
        vo.setInternalRemark(order.getInternalRemark());
        vo.setCancelReason(order.getCancelReason());
        vo.setStatus(order.getStatus().getCode());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        SysUser creator = sysUserMapper.selectById(order.getCreatedBy());
        vo.setCreatedByName(creator != null ? creator.getRealName() : "");

        // 明细
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        vo.setItems(items.stream().map(item -> {
            OrderVO.OrderItemVO iv = new OrderVO.OrderItemVO();
            iv.setId(item.getId());
            iv.setDishId(item.getDishId());
            iv.setDishName(item.getDishName());
            iv.setCategoryName(item.getCategoryName());
            iv.setSpecName(item.getSpecName());
            iv.setUnitPrice(item.getUnitPrice());
            iv.setQuantity(item.getQuantity());
            iv.setAmount(item.getAmount());
            return iv;
        }).toList());

        // 状态日志
        List<OrderStatusLog> logs = orderStatusLogMapper.selectList(
                new LambdaQueryWrapper<OrderStatusLog>().eq(OrderStatusLog::getOrderId, id)
                        .orderByAsc(OrderStatusLog::getCreatedAt));

        // 批量获取操作人姓名
        var operatorIds = logs.stream().map(OrderStatusLog::getOperatorId).distinct().toList();
        Map<Long, String> nameMap = operatorIds.isEmpty() ? java.util.Collections.emptyMap()
                : sysUserMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));

        vo.setStatusLogs(logs.stream().map(log -> {
            OrderVO.OrderStatusLogVO lv = new OrderVO.OrderStatusLogVO();
            lv.setId(log.getId());
            lv.setBeforeStatus(log.getBeforeStatus());
            lv.setAfterStatus(log.getAfterStatus());
            lv.setRemark(log.getRemark());
            lv.setOperatorName(nameMap.getOrDefault(log.getOperatorId(), ""));
            lv.setCreatedAt(log.getCreatedAt());
            return lv;
        }).toList());

        return vo;
    }

    @Override
    public Page<OrderPageVO> queryPage(OrderQueryDTO query) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.eq(Order::getOrderNo, query.getOrderNo());
        }
        if (query.getPickupNo() != null) {
            wrapper.eq(Order::getPickupNo, query.getPickupNo());
        }
        if (StringUtils.hasText(query.getOrderType())) {
            wrapper.eq(Order::getOrderType, OrderType.valueOf(query.getOrderType()));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Order::getStatus, OrderStatus.valueOf(query.getStatus()));
        }
        if (query.getCreatedBy() != null) {
            wrapper.eq(Order::getCreatedBy, query.getCreatedBy());
        }
        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(Order::getCreatedAt, LocalDateTime.of(LocalDate.parse(query.getStartDate()), LocalTime.MIN));
        }
        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(Order::getCreatedAt, LocalDateTime.of(LocalDate.parse(query.getEndDate()), LocalTime.MAX));
        }

        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        var creatorIds = page.getRecords().stream().map(Order::getCreatedBy).distinct().toList();
        Map<Long, String> nameMap;
        if (creatorIds.isEmpty()) {
            nameMap = java.util.Collections.emptyMap();
        } else {
            nameMap = sysUserMapper.selectBatchIds(creatorIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        }

        return (Page<OrderPageVO>) page.convert(order -> {
            OrderPageVO vo = new OrderPageVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setOrderType(order.getOrderType().getCode());
            vo.setTableNo(order.getTableNo());
            vo.setPickupNo(order.getPickupNo());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setStatus(order.getStatus().getCode());
            vo.setCreatedByName(nameMap.getOrDefault(order.getCreatedBy(), ""));
            vo.setCreatedAt(order.getCreatedAt());
            return vo;
        });
    }

    @Override
    @Transactional
    public void changeStatus(Long id, String targetStatus, String remark, Long operatorId) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        OrderStatus from = order.getStatus();
        OrderStatus to = OrderStatus.valueOf(targetStatus);

        // 状态流转校验
        if (from == OrderStatus.PENDING && to == OrderStatus.COOKING) {
            // 确认制作：需要扣减库存
            deductInventory(id, order.getOrderNo(), operatorId);
        } else if (from == OrderStatus.COOKING && to == OrderStatus.READY) {
            // 制作完成
        } else if (from == OrderStatus.READY && to == OrderStatus.COMPLETED) {
            // 完成
        } else {
            throw new BusinessException("不允许从 " + from.getDesc() + " 变更为 " + to.getDesc());
        }

        order.setStatus(to);
        order.setUpdatedBy(operatorId);
        updateById(order);

        saveStatusLog(order.getId(), from.getCode(), to.getCode(), remark, operatorId);
    }

    @Override
    @Transactional
    public void cancel(Long id, String reason, Long operatorId) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("仅待确认订单可以取消");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("取消订单必须填写原因");
        }

        String beforeStatus = order.getStatus().getCode();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setUpdatedBy(operatorId);
        updateById(order);

        saveStatusLog(order.getId(), beforeStatus, OrderStatus.CANCELLED.getCode(),
                "取消原因: " + reason, operatorId);
    }

    @Override
    public byte[] exportExcel(OrderQueryDTO query) {
        // Simplified: delegate to EasyExcel export (see Phase 5 for detailed export)
        return new byte[0];
    }

    /**
     * 扣减库存 - 核心事务方法
     */
    private void deductInventory(Long orderId, String orderNo, Long operatorId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));

        StringBuilder shortageMsg = new StringBuilder();
        List<InventoryRecord> records = new ArrayList<>();

        for (OrderItem item : items) {
            // 查询菜品配方
            List<DishMaterial> recipe = dishMaterialMapper.selectList(
                    new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getDishId, item.getDishId()));

            for (DishMaterial dm : recipe) {
                Material material = materialMapper.selectById(dm.getMaterialId());
                if (material == null) continue;

                // 计算需要数量
                BigDecimal needQty = dm.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal currentStock = material.getCurrentStock();

                if (currentStock.compareTo(needQty) < 0) {
                    shortageMsg.append(material.getName())
                            .append("库存不足：需要 ").append(needQty)
                            .append(" ").append(material.getUnit())
                            .append("，当前库存 ").append(currentStock)
                            .append(" ").append(material.getUnit())
                            .append("；");
                }

                // 乐观锁更新库存
                material.setCurrentStock(currentStock.subtract(needQty));
                material.setUpdatedBy(operatorId);
                if (materialMapper.updateById(material) == 0) {
                    throw new BusinessException("库存更新冲突，请重试");
                }

                // 生成库存流水
                InventoryRecord record = new InventoryRecord();
                record.setMaterialId(material.getId());
                record.setChangeType(InventoryChangeType.ORDER_CONSUME);
                record.setBeforeStock(currentStock);
                record.setChangeQuantity(needQty.negate());
                record.setAfterStock(material.getCurrentStock());
                record.setBusinessNo(orderNo);
                record.setRemark("订单消耗: " + item.getDishName() + " x" + item.getQuantity());
                record.setOperatorId(operatorId);
                records.add(record);
            }
        }

        if (shortageMsg.length() > 0) {
            throw new BusinessException(shortageMsg.toString());
        }

        // 批量插入库存流水
        for (InventoryRecord record : records) {
            inventoryRecordMapper.insert(record);
        }
    }

    private String generateOrderNo() {
        String prefix = "OD" + LocalDateTimeUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        return prefix + RandomUtil.randomNumbers(4);
    }

    private int generatePickupNo() {
        // 当日最大取餐号+1
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        var lastOrder = getOne(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart)
                .orderByDesc(Order::getPickupNo)
                .last("LIMIT 1"));
        return lastOrder != null ? lastOrder.getPickupNo() + 1 : 1;
    }

    private void saveStatusLog(Long orderId, String beforeStatus, String afterStatus,
                                String remark, Long operatorId) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        orderStatusLogMapper.insert(log);
    }
}
