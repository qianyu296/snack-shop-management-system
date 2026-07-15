package com.snackadmin.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.dto.PurchaseOrderQueryDTO;
import com.snackadmin.dto.PurchaseOrderSaveDTO;
import com.snackadmin.entity.*;
import com.snackadmin.enums.InventoryChangeType;
import com.snackadmin.enums.PurchaseStatus;
import com.snackadmin.enums.SupplierStatus;
import com.snackadmin.mapper.*;
import com.snackadmin.service.PurchaseOrderService;
import com.snackadmin.vo.PurchaseOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseOrderService {

    private final PurchaseOrderItemMapper itemMapper;
    private final SupplierMapper supplierMapper;
    private final MaterialMapper materialMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final SysUserMapper sysUserMapper;

    @Override @Transactional
    public PurchaseOrderVO create(PurchaseOrderSaveDTO dto, Long operatorId) {
        Supplier supplier = supplierMapper.selectById(dto.getSupplierId());
        if (supplier == null || supplier.getStatus() != SupplierStatus.ENABLED)
            throw new BusinessException("供应商不存在或已停用");

        PurchaseOrder po = new PurchaseOrder();
        po.setPurchaseNo(generatePurchaseNo());
        po.setSupplierId(dto.getSupplierId());
        po.setPurchaseDate(dto.getPurchaseDate() != null ? dto.getPurchaseDate() : LocalDate.now());
        po.setStatus(PurchaseStatus.DRAFT);
        po.setRemark(dto.getRemark());
        po.setCreatedBy(operatorId); po.setUpdatedBy(operatorId);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderSaveDTO.Item itemDTO : dto.getItems()) {
            Material mat = materialMapper.selectById(itemDTO.getMaterialId());
            if (mat == null) throw new BusinessException("原料不存在");
            BigDecimal amount = itemDTO.getUnitPrice().multiply(itemDTO.getQuantity());
            total = total.add(amount);
        }
        po.setTotalAmount(total);
        save(po);

        for (PurchaseOrderSaveDTO.Item itemDTO : dto.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(po.getId());
            item.setMaterialId(itemDTO.getMaterialId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(itemDTO.getUnitPrice().multiply(itemDTO.getQuantity()));
            itemMapper.insert(item);
        }
        return getDetail(po.getId());
    }

    @Override @Transactional
    public PurchaseOrderVO update(Long id, PurchaseOrderSaveDTO dto, Long operatorId) {
        PurchaseOrder po = getById(id);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (po.getStatus() != PurchaseStatus.DRAFT) throw new BusinessException("仅草稿状态允许修改");

        po.setSupplierId(dto.getSupplierId());
        po.setPurchaseDate(dto.getPurchaseDate());
        po.setRemark(dto.getRemark());
        po.setUpdatedBy(operatorId);

        itemMapper.delete(new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getPurchaseOrderId, id));

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderSaveDTO.Item itemDTO : dto.getItems()) {
            BigDecimal amount = itemDTO.getUnitPrice().multiply(itemDTO.getQuantity());
            total = total.add(amount);
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(id); item.setMaterialId(itemDTO.getMaterialId());
            item.setQuantity(itemDTO.getQuantity()); item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(amount);
            itemMapper.insert(item);
        }
        po.setTotalAmount(total);
        updateById(po);
        return getDetail(id);
    }

    @Override public PurchaseOrderVO getDetail(Long id) {
        PurchaseOrder po = getById(id);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        return toVO(po);
    }

    @Override @Transactional
    public void warehouse(Long id, Long operatorId) {
        PurchaseOrder po = getById(id);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (po.getStatus() != PurchaseStatus.DRAFT) throw new BusinessException("仅草稿状态允许入库");

        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getPurchaseOrderId, id));

        for (PurchaseOrderItem item : items) {
            Material mat = materialMapper.selectById(item.getMaterialId());
            if (mat == null) throw new BusinessException("原料不存在");

            BigDecimal beforeStock = mat.getCurrentStock();
            BigDecimal afterStock = beforeStock.add(item.getQuantity());
            mat.setCurrentStock(afterStock);
            mat.setUpdatedBy(operatorId);
            if (materialMapper.updateById(mat) == 0) throw new BusinessException("库存更新冲突，请重试");

            InventoryRecord record = new InventoryRecord();
            record.setMaterialId(mat.getId());
            record.setChangeType(InventoryChangeType.PURCHASE_IN);
            record.setBeforeStock(beforeStock);
            record.setChangeQuantity(item.getQuantity());
            record.setAfterStock(afterStock);
            record.setBusinessNo(po.getPurchaseNo());
            record.setRemark("采购入库: " + po.getPurchaseNo());
            record.setOperatorId(operatorId);
            inventoryRecordMapper.insert(record);
        }

        po.setStatus(PurchaseStatus.WAREHOUSED);
        po.setUpdatedBy(operatorId);
        updateById(po);
    }

    @Override @Transactional
    public void cancel(Long id, String reason, Long operatorId) {
        PurchaseOrder po = getById(id);
        if (po == null) throw new BusinessException(404, "采购单不存在");
        if (po.getStatus() != PurchaseStatus.DRAFT) throw new BusinessException("仅草稿状态允许作废");
        if (!StringUtils.hasText(reason)) throw new BusinessException("作废必须填写原因");
        po.setStatus(PurchaseStatus.CANCELLED);
        po.setCancelReason(reason);
        po.setUpdatedBy(operatorId);
        updateById(po);
    }

    @Override
    public Page<PurchaseOrderVO> queryPage(PurchaseOrderQueryDTO query) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getPurchaseNo()), PurchaseOrder::getPurchaseNo, query.getPurchaseNo())
                .eq(query.getSupplierId() != null, PurchaseOrder::getSupplierId, query.getSupplierId())
                .eq(StringUtils.hasText(query.getStatus()), PurchaseOrder::getStatus,
                        StringUtils.hasText(query.getStatus()) ? PurchaseStatus.valueOf(query.getStatus()) : null)
                .orderByDesc(PurchaseOrder::getCreatedAt);
        if (StringUtils.hasText(query.getStartDate()))
            wrapper.ge(PurchaseOrder::getCreatedAt, LocalDateTime.of(LocalDate.parse(query.getStartDate()), LocalTime.MIN));
        if (StringUtils.hasText(query.getEndDate()))
            wrapper.le(PurchaseOrder::getCreatedAt, LocalDateTime.of(LocalDate.parse(query.getEndDate()), LocalTime.MAX));

        Page<PurchaseOrder> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        Map<Long, String> supplierMap = supplierMapper.selectList(null).stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName, (a, b) -> a));
        Map<Long, String> userMap = sysUserMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));

        return (Page<PurchaseOrderVO>) page.convert(po -> {
            PurchaseOrderVO vo = new PurchaseOrderVO();
            vo.setId(po.getId()); vo.setPurchaseNo(po.getPurchaseNo());
            vo.setSupplierId(po.getSupplierId()); vo.setSupplierName(supplierMap.getOrDefault(po.getSupplierId(), ""));
            vo.setPurchaseDate(po.getPurchaseDate()); vo.setTotalAmount(po.getTotalAmount());
            vo.setStatus(po.getStatus().getCode()); vo.setCancelReason(po.getCancelReason());
            vo.setRemark(po.getRemark()); vo.setCreatedByName(userMap.getOrDefault(po.getCreatedBy(), ""));
            vo.setCreatedAt(po.getCreatedAt()); vo.setUpdatedAt(po.getUpdatedAt());
            return vo;
        });
    }

    private PurchaseOrderVO toVO(PurchaseOrder po) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(po.getId()); vo.setPurchaseNo(po.getPurchaseNo());
        vo.setSupplierId(po.getSupplierId());
        Supplier s = supplierMapper.selectById(po.getSupplierId());
        vo.setSupplierName(s != null ? s.getName() : "");
        vo.setPurchaseDate(po.getPurchaseDate()); vo.setTotalAmount(po.getTotalAmount());
        vo.setStatus(po.getStatus().getCode()); vo.setCancelReason(po.getCancelReason());
        vo.setRemark(po.getRemark());
        SysUser u = sysUserMapper.selectById(po.getCreatedBy());
        vo.setCreatedByName(u != null ? u.getRealName() : "");
        vo.setCreatedAt(po.getCreatedAt()); vo.setUpdatedAt(po.getUpdatedAt());

        List<PurchaseOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getPurchaseOrderId, po.getId()));
        vo.setItems(items.stream().map(item -> {
            PurchaseOrderVO.Item iv = new PurchaseOrderVO.Item();
            iv.setId(item.getId()); iv.setMaterialId(item.getMaterialId());
            Material m = materialMapper.selectById(item.getMaterialId());
            iv.setMaterialName(m != null ? m.getName() : ""); iv.setMaterialUnit(m != null ? m.getUnit() : "");
            iv.setQuantity(item.getQuantity()); iv.setUnitPrice(item.getUnitPrice()); iv.setAmount(item.getAmount());
            return iv;
        }).toList());
        return vo;
    }

    private String generatePurchaseNo() {
        return "PO" + LocalDateTimeUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
    }
}
