package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.dto.InventoryRecordQueryDTO;
import com.snackadmin.entity.InventoryRecord;
import com.snackadmin.entity.Material;
import com.snackadmin.entity.SysUser;
import com.snackadmin.enums.InventoryChangeType;
import com.snackadmin.mapper.InventoryRecordMapper;
import com.snackadmin.mapper.MaterialMapper;
import com.snackadmin.mapper.SysUserMapper;
import com.snackadmin.service.InventoryRecordService;
import com.snackadmin.vo.InventoryRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存流水服务实现
 */
@Service
@RequiredArgsConstructor
public class InventoryRecordServiceImpl extends ServiceImpl<InventoryRecordMapper, InventoryRecord>
        implements InventoryRecordService {

    private final MaterialMapper materialMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Page<InventoryRecordVO> queryPage(InventoryRecordQueryDTO query) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getMaterialId() != null, InventoryRecord::getMaterialId, query.getMaterialId())
                .eq(StringUtils.hasText(query.getChangeType()), InventoryRecord::getChangeType,
                        StringUtils.hasText(query.getChangeType())
                                ? InventoryChangeType.valueOf(query.getChangeType()) : null)
                .eq(StringUtils.hasText(query.getBusinessNo()), InventoryRecord::getBusinessNo, query.getBusinessNo())
                .orderByDesc(InventoryRecord::getCreatedAt);

        // 日期范围
        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(InventoryRecord::getCreatedAt,
                    LocalDateTime.of(LocalDate.parse(query.getStartDate()), LocalTime.MIN));
        }
        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(InventoryRecord::getCreatedAt,
                    LocalDateTime.of(LocalDate.parse(query.getEndDate()), LocalTime.MAX));
        }

        Page<InventoryRecord> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 批量获取原料信息
        var materialIds = page.getRecords().stream()
                .map(InventoryRecord::getMaterialId).distinct().toList();
        Map<Long, Material> materialMap = materialIds.isEmpty() ? java.util.Collections.emptyMap()
                : materialMapper.selectBatchIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, m -> m, (a, b) -> a));

        // 批量获取操作人信息
        var operatorIds = page.getRecords().stream()
                .map(InventoryRecord::getOperatorId).distinct().toList();
        Map<Long, SysUser> userMap = operatorIds.isEmpty() ? java.util.Collections.emptyMap()
                : sysUserMapper.selectBatchIds(operatorIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        return (Page<InventoryRecordVO>) page.convert(record -> {
            InventoryRecordVO vo = new InventoryRecordVO();
            vo.setId(record.getId());
            vo.setMaterialId(record.getMaterialId());
            Material m = materialMap.get(record.getMaterialId());
            if (m != null) {
                vo.setMaterialName(m.getName());
                vo.setMaterialUnit(m.getUnit());
            }
            vo.setChangeType(record.getChangeType().getCode());
            vo.setBeforeStock(record.getBeforeStock());
            vo.setChangeQuantity(record.getChangeQuantity());
            vo.setAfterStock(record.getAfterStock());
            vo.setBusinessNo(record.getBusinessNo());
            vo.setRemark(record.getRemark());
            SysUser u = userMap.get(record.getOperatorId());
            vo.setOperatorName(u != null ? u.getRealName() : "");
            vo.setCreatedAt(record.getCreatedAt());
            return vo;
        });
    }
}
