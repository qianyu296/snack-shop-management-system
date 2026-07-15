package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.MaterialSaveDTO;
import com.snackadmin.dto.StockAdjustDTO;
import com.snackadmin.entity.*;
import com.snackadmin.enums.InventoryChangeType;
import com.snackadmin.enums.MaterialCategory;
import com.snackadmin.enums.MaterialStatus;
import com.snackadmin.mapper.*;
import com.snackadmin.service.MaterialService;
import com.snackadmin.vo.MaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 原料服务实现
 */
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    private final DishMaterialMapper dishMaterialMapper;
    private final DishMapper dishMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Override
    public Page<MaterialVO> queryPage(PageQuery pageQuery, String name, String category,
                                       String status, Boolean lowStock) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Material::getName, name)
                .eq(StringUtils.hasText(category), Material::getCategory,
                        StringUtils.hasText(category) ? MaterialCategory.valueOf(category) : null)
                .eq(StringUtils.hasText(status), Material::getStatus,
                        StringUtils.hasText(status) ? MaterialStatus.valueOf(status) : null)
                .orderByDesc(Material::getCreatedAt);

        // 低库存筛选: 使用le条件
        if (Boolean.TRUE.equals(lowStock)) {
            wrapper.apply("current_stock <= safe_stock");
        }

        Page<Material> page = page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);

        return (Page<MaterialVO>) page.convert(this::toVO);
    }

    @Override
    public MaterialVO getDetail(Long id) {
        Material material = getById(id);
        if (material == null) {
            throw new BusinessException(404, "原料不存在");
        }
        return toVO(material);
    }

    @Override
    @Transactional
    public void create(MaterialSaveDTO dto, Long operatorId) {
        LambdaQueryWrapper<Material> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Material::getName, dto.getName());
        if (exists(nameWrapper)) {
            throw new BusinessException(409, "原料名称已存在");
        }

        Material material = new Material();
        material.setName(dto.getName());
        material.setCategory(MaterialCategory.valueOf(dto.getCategory()));
        material.setUnit(dto.getUnit());
        material.setCurrentStock(BigDecimal.ZERO);
        material.setSafeStock(dto.getSafeStock());
        material.setStatus(MaterialStatus.valueOf(dto.getStatus()));
        material.setRemark(dto.getRemark());
        material.setCreatedBy(operatorId);
        material.setUpdatedBy(operatorId);
        save(material);
    }

    @Override
    @Transactional
    public void update(Long id, MaterialSaveDTO dto, Long operatorId) {
        Material material = getById(id);
        if (material == null) {
            throw new BusinessException(404, "原料不存在");
        }

        // 校验名称唯一
        LambdaQueryWrapper<Material> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Material::getName, dto.getName()).ne(Material::getId, id);
        if (exists(nameWrapper)) {
            throw new BusinessException(409, "原料名称已存在");
        }

        // 单位被业务引用后不可修改
        boolean isReferenced = dishMaterialMapper.exists(
                new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getMaterialId, id));
        if (isReferenced && !material.getUnit().equals(dto.getUnit())) {
            throw new BusinessException("该原料已被菜品配方引用，不可修改计量单位");
        }

        material.setName(dto.getName());
        material.setCategory(MaterialCategory.valueOf(dto.getCategory()));
        material.setUnit(dto.getUnit());
        material.setSafeStock(dto.getSafeStock());
        material.setStatus(MaterialStatus.valueOf(dto.getStatus()));
        material.setRemark(dto.getRemark());
        material.setUpdatedBy(operatorId);
        updateById(material);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, Long operatorId) {
        Material material = getById(id);
        if (material == null) {
            throw new BusinessException(404, "原料不存在");
        }

        MaterialStatus targetStatus = MaterialStatus.valueOf(status);
        if (targetStatus == MaterialStatus.DISABLED) {
            // 被上架菜品配方引用时不允许停用
            LambdaQueryWrapper<DishMaterial> refWrapper = new LambdaQueryWrapper<>();
            refWrapper.eq(DishMaterial::getMaterialId, id)
                    .apply("dish_id IN (SELECT id FROM dish WHERE sale_status = 'ON_SALE' AND deleted = 0)");
            if (dishMaterialMapper.exists(refWrapper)) {
                throw new BusinessException("该原料被上架菜品配方引用，不允许停用");
            }
        }

        material.setStatus(targetStatus);
        material.setUpdatedBy(operatorId);
        updateById(material);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 无配方引用才允许删除
        if (dishMaterialMapper.exists(
                new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getMaterialId, id))) {
            throw new BusinessException("该原料被菜品配方引用，无法删除");
        }
        // 无采购明细引用
        LambdaQueryWrapper<PurchaseOrderItem> poiWrapper = new LambdaQueryWrapper<>();
        poiWrapper.eq(PurchaseOrderItem::getMaterialId, id).last("LIMIT 1");
        if (purchaseOrderItemMapper.selectCount(poiWrapper) > 0) {
            throw new BusinessException("该原料存在采购记录，无法删除");
        }
        // 无库存流水
        if (inventoryRecordMapper.exists(
                new LambdaQueryWrapper<InventoryRecord>().eq(InventoryRecord::getMaterialId, id))) {
            throw new BusinessException("该原料存在库存流水，无法删除");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void adjustStock(StockAdjustDTO dto, Long operatorId) {
        Material material = getById(dto.getMaterialId());
        if (material == null) {
            throw new BusinessException(404, "原料不存在");
        }

        InventoryChangeType changeType = InventoryChangeType.valueOf(dto.getChangeType());

        BigDecimal beforeStock = material.getCurrentStock();
        BigDecimal changeQuantity = dto.getQuantity();
        BigDecimal afterStock;

        if (changeType == InventoryChangeType.SURPLUS_ADJUST) {
            afterStock = beforeStock.add(changeQuantity);
        } else if (changeType == InventoryChangeType.LOSS_ADJUST) {
            afterStock = beforeStock.subtract(changeQuantity);
            if (afterStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("盘亏数量不能大于当前库存（当前库存: " + beforeStock + "）");
            }
        } else {
            throw new BusinessException("不支持的调整类型");
        }

        // 乐观锁更新
        material.setCurrentStock(afterStock);
        material.setUpdatedBy(operatorId);
        if (!updateById(material)) {
            throw new BusinessException("库存更新失败，请重试");
        }

        // 生成库存流水
        InventoryRecord record = new InventoryRecord();
        record.setMaterialId(material.getId());
        record.setChangeType(changeType);
        record.setBeforeStock(beforeStock);
        record.setChangeQuantity(changeType == InventoryChangeType.LOSS_ADJUST
                ? changeQuantity.negate() : changeQuantity);
        record.setAfterStock(afterStock);
        record.setBusinessNo(dto.getBusinessNo());
        record.setRemark(dto.getRemark());
        record.setOperatorId(operatorId);
        inventoryRecordMapper.insert(record);
    }

    private MaterialVO toVO(Material m) {
        MaterialVO vo = new MaterialVO();
        vo.setId(m.getId());
        vo.setName(m.getName());
        vo.setCategory(m.getCategory().getCode());
        vo.setUnit(m.getUnit());
        vo.setCurrentStock(m.getCurrentStock());
        vo.setSafeStock(m.getSafeStock());
        vo.setStatus(m.getStatus().getCode());
        vo.setRemark(m.getRemark());
        vo.setLowStock(m.getCurrentStock().compareTo(m.getSafeStock()) <= 0);
        vo.setCreatedAt(m.getCreatedAt());
        vo.setUpdatedAt(m.getUpdatedAt());
        return vo;
    }
}
