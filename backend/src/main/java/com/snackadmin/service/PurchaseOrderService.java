package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.dto.PurchaseOrderQueryDTO;
import com.snackadmin.dto.PurchaseOrderSaveDTO;
import com.snackadmin.entity.PurchaseOrder;
import com.snackadmin.vo.PurchaseOrderVO;

public interface PurchaseOrderService extends IService<PurchaseOrder> {
    PurchaseOrderVO create(PurchaseOrderSaveDTO dto, Long operatorId);
    PurchaseOrderVO update(Long id, PurchaseOrderSaveDTO dto, Long operatorId);
    PurchaseOrderVO getDetail(Long id);
    Page<PurchaseOrderVO> queryPage(PurchaseOrderQueryDTO query);
    void warehouse(Long id, Long operatorId);
    void cancel(Long id, String reason, Long operatorId);
}
