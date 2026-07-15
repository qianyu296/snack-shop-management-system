package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.DishSaveDTO;
import com.snackadmin.entity.Dish;
import com.snackadmin.vo.DishVO;
import com.snackadmin.vo.DishPageVO;

import java.util.List;

/**
 * 菜品服务
 */
public interface DishService extends IService<Dish> {

    Page<DishPageVO> queryPage(PageQuery pageQuery, String name, Long categoryId,
                                String saleStatus, String recommendStatus);

    DishVO getDetail(Long id);

    void create(DishSaveDTO dto, Long operatorId);

    void update(Long id, DishSaveDTO dto, Long operatorId);

    void updateSaleStatus(Long id, String saleStatus, Long operatorId);

    void updateRecommendStatus(Long id, String recommendStatus, Long operatorId);

    void delete(Long id);

    /** 获取已上架菜品列表（供订单创建使用） */
    List<DishVO> getOnSaleDishes();
}
