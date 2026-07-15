package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.dto.SupplierQueryDTO;
import com.snackadmin.dto.SupplierSaveDTO;
import com.snackadmin.entity.PurchaseOrder;
import com.snackadmin.entity.Supplier;
import com.snackadmin.enums.SupplierStatus;
import com.snackadmin.mapper.PurchaseOrderMapper;
import com.snackadmin.mapper.SupplierMapper;
import com.snackadmin.service.SupplierService;
import com.snackadmin.vo.SupplierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public Page<SupplierVO> queryPage(SupplierQueryDTO query) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), Supplier::getName, query.getName())
                .like(StringUtils.hasText(query.getContactName()), Supplier::getContactName, query.getContactName())
                .like(StringUtils.hasText(query.getContactPhone()), Supplier::getContactPhone, query.getContactPhone())
                .eq(StringUtils.hasText(query.getStatus()), Supplier::getStatus,
                        StringUtils.hasText(query.getStatus()) ? SupplierStatus.valueOf(query.getStatus()) : null)
                .orderByDesc(Supplier::getCreatedAt);
        Page<Supplier> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return (Page<SupplierVO>) page.convert(this::toVO);
    }

    @Override public SupplierVO getDetail(Long id) {
        Supplier s = getById(id); if (s == null) throw new BusinessException(404, "供应商不存在"); return toVO(s);
    }

    @Override @Transactional
    public void create(SupplierSaveDTO dto, Long operatorId) {
        if (exists(new LambdaQueryWrapper<Supplier>().eq(Supplier::getName, dto.getName())))
            throw new BusinessException(409, "供应商名称已存在");
        Supplier s = new Supplier();
        s.setName(dto.getName()); s.setContactName(dto.getContactName());
        s.setContactPhone(dto.getContactPhone()); s.setAddress(dto.getAddress());
        s.setSupplyMaterials(dto.getSupplyMaterials()); s.setStatus(SupplierStatus.valueOf(dto.getStatus()));
        s.setRemark(dto.getRemark()); s.setCreatedBy(operatorId); s.setUpdatedBy(operatorId);
        save(s);
    }

    @Override @Transactional
    public void update(Long id, SupplierSaveDTO dto, Long operatorId) {
        Supplier s = getById(id); if (s == null) throw new BusinessException(404, "供应商不存在");
        if (exists(new LambdaQueryWrapper<Supplier>().eq(Supplier::getName, dto.getName()).ne(Supplier::getId, id)))
            throw new BusinessException(409, "供应商名称已存在");
        s.setName(dto.getName()); s.setContactName(dto.getContactName());
        s.setContactPhone(dto.getContactPhone()); s.setAddress(dto.getAddress());
        s.setSupplyMaterials(dto.getSupplyMaterials()); s.setStatus(SupplierStatus.valueOf(dto.getStatus()));
        s.setRemark(dto.getRemark()); s.setUpdatedBy(operatorId);
        updateById(s);
    }

    @Override @Transactional
    public void updateStatus(Long id, String status, Long operatorId) {
        Supplier s = getById(id); if (s == null) throw new BusinessException(404, "供应商不存在");
        s.setStatus(SupplierStatus.valueOf(status)); s.setUpdatedBy(operatorId); updateById(s);
    }

    @Override @Transactional
    public void delete(Long id) {
        if (purchaseOrderMapper.exists(new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getSupplierId, id)))
            throw new BusinessException("该供应商存在采购单引用，无法删除");
        removeById(id);
    }

    private SupplierVO toVO(Supplier s) {
        SupplierVO vo = new SupplierVO();
        vo.setId(s.getId()); vo.setName(s.getName()); vo.setContactName(s.getContactName());
        vo.setContactPhone(s.getContactPhone()); vo.setAddress(s.getAddress());
        vo.setSupplyMaterials(s.getSupplyMaterials()); vo.setStatus(s.getStatus().getCode());
        vo.setRemark(s.getRemark()); vo.setCreatedAt(s.getCreatedAt()); vo.setUpdatedAt(s.getUpdatedAt());
        return vo;
    }
}
