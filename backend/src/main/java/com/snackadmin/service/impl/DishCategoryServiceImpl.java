package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.DishCategorySaveDTO;
import com.snackadmin.entity.Dish;
import com.snackadmin.entity.DishCategory;
import com.snackadmin.enums.CategoryStatus;
import com.snackadmin.mapper.DishCategoryMapper;
import com.snackadmin.mapper.DishMapper;
import com.snackadmin.service.DishCategoryService;
import com.snackadmin.vo.DishCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 菜品分类服务实现
 */
@Service
@RequiredArgsConstructor
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory>
        implements DishCategoryService {

    private final DishMapper dishMapper;

    @Override
    public Page<DishCategoryVO> queryPage(PageQuery pageQuery, String name, String status) {
        LambdaQueryWrapper<DishCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), DishCategory::getName, name)
                .eq(StringUtils.hasText(status), DishCategory::getStatus,
                        StringUtils.hasText(status) ? CategoryStatus.valueOf(status) : null)
                .orderByAsc(DishCategory::getSort)
                .orderByAsc(DishCategory::getCreatedAt);

        Page<DishCategory> page = page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);

        return (Page<DishCategoryVO>) page.convert(cat -> {
            DishCategoryVO vo = toVO(cat);
            // 统计菜品数量
            Long dishCount = dishMapper.selectCount(
                    new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, cat.getId()));
            vo.setDishCount(dishCount);
            return vo;
        });
    }

    @Override
    public DishCategoryVO getDetail(Long id) {
        DishCategory cat = getById(id);
        if (cat == null) {
            throw new BusinessException(404, "分类不存在");
        }
        DishCategoryVO vo = toVO(cat);
        Long dishCount = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, cat.getId()));
        vo.setDishCount(dishCount);
        return vo;
    }

    @Override
    @Transactional
    public void create(DishCategorySaveDTO dto, Long operatorId) {
        // 校验名称唯一
        if (exists(new LambdaQueryWrapper<DishCategory>().eq(DishCategory::getName, dto.getName()))) {
            throw new BusinessException(409, "分类名称已存在");
        }

        DishCategory cat = new DishCategory();
        cat.setName(dto.getName());
        cat.setSort(dto.getSort() != null ? dto.getSort() : 0);
        cat.setStatus(CategoryStatus.valueOf(dto.getStatus()));
        cat.setRemark(dto.getRemark());
        cat.setCreatedBy(operatorId);
        cat.setUpdatedBy(operatorId);
        save(cat);
    }

    @Override
    @Transactional
    public void update(Long id, DishCategorySaveDTO dto, Long operatorId) {
        DishCategory cat = getById(id);
        if (cat == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // 校验名称唯一（排除自己）
        if (exists(new LambdaQueryWrapper<DishCategory>()
                .eq(DishCategory::getName, dto.getName())
                .ne(DishCategory::getId, id))) {
            throw new BusinessException(409, "分类名称已存在");
        }

        cat.setName(dto.getName());
        cat.setSort(dto.getSort());
        cat.setStatus(CategoryStatus.valueOf(dto.getStatus()));
        cat.setRemark(dto.getRemark());
        cat.setUpdatedBy(operatorId);
        updateById(cat);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, Long operatorId) {
        DishCategory cat = getById(id);
        if (cat == null) {
            throw new BusinessException(404, "分类不存在");
        }
        cat.setStatus(CategoryStatus.valueOf(status));
        cat.setUpdatedBy(operatorId);
        updateById(cat);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 校验分类下无菜品
        Long dishCount = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (dishCount > 0) {
            throw new BusinessException("该分类下存在菜品，无法删除");
        }
        removeById(id);
    }

    private DishCategoryVO toVO(DishCategory cat) {
        DishCategoryVO vo = new DishCategoryVO();
        vo.setId(cat.getId());
        vo.setName(cat.getName());
        vo.setSort(cat.getSort());
        vo.setStatus(cat.getStatus().getCode());
        vo.setRemark(cat.getRemark());
        vo.setCreatedAt(cat.getCreatedAt());
        vo.setUpdatedAt(cat.getUpdatedAt());
        return vo;
    }
}
