package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.dto.SupplierQueryDTO;
import com.snackadmin.dto.SupplierSaveDTO;
import com.snackadmin.entity.Supplier;
import com.snackadmin.vo.SupplierVO;

public interface SupplierService extends IService<Supplier> {
    Page<SupplierVO> queryPage(SupplierQueryDTO query);
    SupplierVO getDetail(Long id);
    void create(SupplierSaveDTO dto, Long operatorId);
    void update(Long id, SupplierSaveDTO dto, Long operatorId);
    void updateStatus(Long id, String status, Long operatorId);
    void delete(Long id);
}
