package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.common.PageQuery;
import com.snackadmin.entity.SysOperationLog;

public interface OperationLogService extends IService<SysOperationLog> {
    void saveLog(SysOperationLog log);
    Page<SysOperationLog> queryPage(PageQuery pageQuery, String module, String result, String startDate, String endDate);
}
