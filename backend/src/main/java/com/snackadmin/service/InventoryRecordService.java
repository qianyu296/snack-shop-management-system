package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.dto.InventoryRecordQueryDTO;
import com.snackadmin.entity.InventoryRecord;
import com.snackadmin.vo.InventoryRecordVO;

/**
 * 库存流水服务
 */
public interface InventoryRecordService extends IService<InventoryRecord> {

    Page<InventoryRecordVO> queryPage(InventoryRecordQueryDTO query);
}
