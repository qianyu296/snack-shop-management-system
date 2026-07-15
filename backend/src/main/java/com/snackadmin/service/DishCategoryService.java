package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.DishCategorySaveDTO;
import com.snackadmin.entity.DishCategory;
import com.snackadmin.vo.DishCategoryVO;

/**
 * 菜品分类服务
 */
public interface DishCategoryService extends IService<DishCategory> {

    Page<DishCategoryVO> queryPage(PageQuery pageQuery, String name, String status);

    DishCategoryVO getDetail(Long id);

    void create(DishCategorySaveDTO dto, Long operatorId);

    void update(Long id, DishCategorySaveDTO dto, Long operatorId);

    void updateStatus(Long id, String status, Long operatorId);

    void delete(Long id);
}
