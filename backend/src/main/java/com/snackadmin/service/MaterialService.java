package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.MaterialSaveDTO;
import com.snackadmin.dto.StockAdjustDTO;
import com.snackadmin.entity.Material;
import com.snackadmin.vo.MaterialVO;

/**
 * 原料服务
 */
public interface MaterialService extends IService<Material> {

    Page<MaterialVO> queryPage(PageQuery pageQuery, String name, String category, String status, Boolean lowStock);

    MaterialVO getDetail(Long id);

    void create(MaterialSaveDTO dto, Long operatorId);

    void update(Long id, MaterialSaveDTO dto, Long operatorId);

    void updateStatus(Long id, String status, Long operatorId);

    void delete(Long id);

    void adjustStock(StockAdjustDTO dto, Long operatorId);
}
