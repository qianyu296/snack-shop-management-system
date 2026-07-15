package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.DishSaveDTO;
import com.snackadmin.entity.*;
import com.snackadmin.enums.*;
import com.snackadmin.mapper.*;
import com.snackadmin.service.DishService;
import com.snackadmin.vo.DishVO;
import com.snackadmin.vo.DishPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品服务实现
 */
@Service
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishSpecMapper dishSpecMapper;
    private final DishMaterialMapper dishMaterialMapper;
    private final DishCategoryMapper dishCategoryMapper;
    private final MaterialMapper materialMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Page<DishPageVO> queryPage(PageQuery pageQuery, String name, Long categoryId,
                                       String saleStatus, String recommendStatus) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Dish::getName, name)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(StringUtils.hasText(saleStatus), Dish::getSaleStatus,
                        StringUtils.hasText(saleStatus) ? DishSaleStatus.valueOf(saleStatus) : null)
                .eq(StringUtils.hasText(recommendStatus), Dish::getRecommendStatus,
                        StringUtils.hasText(recommendStatus) ? RecommendStatus.valueOf(recommendStatus) : null)
                .orderByDesc(Dish::getCreatedAt);

        Page<Dish> page = page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);

        return (Page<DishPageVO>) page.convert(dish -> {
            DishPageVO vo = new DishPageVO();
            vo.setId(dish.getId());
            vo.setCategoryId(dish.getCategoryId());
            DishCategory cat = dishCategoryMapper.selectById(dish.getCategoryId());
            vo.setCategoryName(cat != null ? cat.getName() : "");
            vo.setName(dish.getName());
            vo.setBasePrice(dish.getBasePrice());
            vo.setImageUrl(dish.getImageUrl());
            vo.setTaste(dish.getTaste());
            vo.setRecommendStatus(dish.getRecommendStatus().getCode());
            vo.setSaleStatus(dish.getSaleStatus().getCode());
            vo.setCreatedAt(dish.getCreatedAt());
            vo.setUpdatedAt(dish.getUpdatedAt());
            return vo;
        });
    }

    @Override
    public DishVO getDetail(Long id) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BusinessException(404, "菜品不存在");
        }
        return toVO(dish);
    }

    @Override
    @Transactional
    public void create(DishSaveDTO dto, Long operatorId) {
        // 校验分类存在
        DishCategory category = dishCategoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("所选分类不存在");
        }

        // 校验同一分类下名称不重复
        if (exists(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, dto.getCategoryId())
                .eq(Dish::getName, dto.getName()))) {
            throw new BusinessException(409, "该分类下已存在同名菜品");
        }

        Dish dish = new Dish();
        dish.setCategoryId(dto.getCategoryId());
        dish.setName(dto.getName());
        dish.setBasePrice(dto.getBasePrice());
        dish.setImageUrl(dto.getImageUrl());
        dish.setTaste(dto.getTaste());
        dish.setDescription(dto.getDescription());
        dish.setRecommendStatus(RecommendStatus.valueOf(dto.getRecommendStatus()));
        dish.setSaleStatus(DishSaleStatus.valueOf(dto.getSaleStatus()));
        dish.setCreatedBy(operatorId);
        dish.setUpdatedBy(operatorId);
        save(dish);

        // 保存规格
        saveSpecs(dish.getId(), dto.getSpecs());

        // 保存配方
        saveMaterials(dish.getId(), dto.getMaterials());
    }

    @Override
    @Transactional
    public void update(Long id, DishSaveDTO dto, Long operatorId) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BusinessException(404, "菜品不存在");
        }

        // 校验分类
        DishCategory category = dishCategoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("所选分类不存在");
        }

        // 校验名称
        if (exists(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, dto.getCategoryId())
                .eq(Dish::getName, dto.getName())
                .ne(Dish::getId, id))) {
            throw new BusinessException(409, "该分类下已存在同名菜品");
        }

        dish.setCategoryId(dto.getCategoryId());
        dish.setName(dto.getName());
        dish.setBasePrice(dto.getBasePrice());
        dish.setImageUrl(dto.getImageUrl());
        dish.setTaste(dto.getTaste());
        dish.setDescription(dto.getDescription());
        dish.setRecommendStatus(RecommendStatus.valueOf(dto.getRecommendStatus()));
        dish.setSaleStatus(DishSaleStatus.valueOf(dto.getSaleStatus()));
        dish.setUpdatedBy(operatorId);
        updateById(dish);

        // 删除旧规格和配方，重新保存
        dishSpecMapper.delete(new LambdaQueryWrapper<DishSpec>().eq(DishSpec::getDishId, id));
        dishMaterialMapper.delete(new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getDishId, id));
        saveSpecs(id, dto.getSpecs());
        saveMaterials(id, dto.getMaterials());
    }

    @Override
    @Transactional
    public void updateSaleStatus(Long id, String saleStatus, Long operatorId) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BusinessException(404, "菜品不存在");
        }

        DishSaleStatus targetStatus = DishSaleStatus.valueOf(saleStatus);

        if (targetStatus == DishSaleStatus.ON_SALE) {
            // 上架校验
            DishCategory category = dishCategoryMapper.selectById(dish.getCategoryId());
            if (category == null || category.getStatus() == CategoryStatus.DISABLED) {
                throw new BusinessException("所选分类已停用，无法上架");
            }

            if (dish.getBasePrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("菜品价格必须大于0，无法上架");
            }

            // 校验配方完整性
            List<DishMaterial> materials = dishMaterialMapper.selectList(
                    new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getDishId, id));
            if (materials.isEmpty()) {
                throw new BusinessException("请先配置菜品配方，无法上架");
            }
            // 校验关联原料未删除
            for (DishMaterial dm : materials) {
                Material m = materialMapper.selectById(dm.getMaterialId());
                if (m == null) {
                    throw new BusinessException("配方中存在已删除的原料，请先更新配方");
                }
            }
        }

        dish.setSaleStatus(targetStatus);
        dish.setUpdatedBy(operatorId);
        updateById(dish);
    }

    @Override
    @Transactional
    public void updateRecommendStatus(Long id, String recommendStatus, Long operatorId) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BusinessException(404, "菜品不存在");
        }
        dish.setRecommendStatus(RecommendStatus.valueOf(recommendStatus));
        dish.setUpdatedBy(operatorId);
        updateById(dish);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new BusinessException(404, "菜品不存在");
        }

        // 仅草稿或已下架允许删除
        if (dish.getSaleStatus() != DishSaleStatus.DRAFT
                && dish.getSaleStatus() != DishSaleStatus.OFF_SALE) {
            throw new BusinessException("仅草稿或已下架菜品允许删除");
        }

        // 存在订单记录时不允许删除
        if (orderItemMapper.exists(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getDishId, id))) {
            throw new BusinessException("该菜品存在订单记录，无法删除");
        }

        // 删除关联规格和配方
        dishSpecMapper.delete(new LambdaQueryWrapper<DishSpec>().eq(DishSpec::getDishId, id));
        dishMaterialMapper.delete(new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getDishId, id));

        removeById(id);
    }

    @Override
    public List<DishVO> getOnSaleDishes() {
        List<Dish> dishes = list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getSaleStatus, DishSaleStatus.ON_SALE)
                .orderByDesc(Dish::getCreatedAt));
        return dishes.stream().map(this::toVO).collect(Collectors.toList());
    }

    private void saveSpecs(Long dishId, List<DishSaveDTO.DishSpecDTO> specDTOs) {
        if (specDTOs != null && !specDTOs.isEmpty()) {
            for (DishSaveDTO.DishSpecDTO specDTO : specDTOs) {
                DishSpec spec = new DishSpec();
                spec.setDishId(dishId);
                spec.setName(specDTO.getName());
                spec.setPrice(specDTO.getPrice());
                dishSpecMapper.insert(spec);
            }
        }
    }

    private void saveMaterials(Long dishId, List<DishSaveDTO.DishMaterialDTO> materialDTOs) {
        if (materialDTOs != null && !materialDTOs.isEmpty()) {
            for (DishSaveDTO.DishMaterialDTO mDTO : materialDTOs) {
                // 校验原料存在
                Material material = materialMapper.selectById(mDTO.getMaterialId());
                if (material == null) {
                    throw new BusinessException("原料ID " + mDTO.getMaterialId() + " 不存在");
                }
                DishMaterial dm = new DishMaterial();
                dm.setDishId(dishId);
                dm.setMaterialId(mDTO.getMaterialId());
                dm.setQuantity(mDTO.getQuantity());
                dishMaterialMapper.insert(dm);
            }
        }
    }

    private DishVO toVO(Dish dish) {
        DishVO vo = new DishVO();
        vo.setId(dish.getId());
        vo.setCategoryId(dish.getCategoryId());
        DishCategory cat = dishCategoryMapper.selectById(dish.getCategoryId());
        vo.setCategoryName(cat != null ? cat.getName() : "");
        vo.setName(dish.getName());
        vo.setBasePrice(dish.getBasePrice());
        vo.setImageUrl(dish.getImageUrl());
        vo.setTaste(dish.getTaste());
        vo.setDescription(dish.getDescription());
        vo.setRecommendStatus(dish.getRecommendStatus().getCode());
        vo.setSaleStatus(dish.getSaleStatus().getCode());
        vo.setCreatedAt(dish.getCreatedAt());
        vo.setUpdatedAt(dish.getUpdatedAt());

        // 规格
        List<DishSpec> specs = dishSpecMapper.selectList(
                new LambdaQueryWrapper<DishSpec>().eq(DishSpec::getDishId, dish.getId()));
        vo.setSpecs(specs.stream().map(s -> {
            DishVO.SpecVO sv = new DishVO.SpecVO();
            sv.setId(s.getId());
            sv.setName(s.getName());
            sv.setPrice(s.getPrice());
            return sv;
        }).toList());

        // 配方
        List<DishMaterial> materials = dishMaterialMapper.selectList(
                new LambdaQueryWrapper<DishMaterial>().eq(DishMaterial::getDishId, dish.getId()));
        vo.setMaterials(materials.stream().map(m -> {
            DishVO.MaterialRefVO mv = new DishVO.MaterialRefVO();
            mv.setId(m.getId());
            mv.setMaterialId(m.getMaterialId());
            Material mat = materialMapper.selectById(m.getMaterialId());
            mv.setMaterialName(mat != null ? mat.getName() : "");
            mv.setMaterialUnit(mat != null ? mat.getUnit() : "");
            mv.setQuantity(m.getQuantity());
            return mv;
        }).toList());

        return vo;
    }
}
