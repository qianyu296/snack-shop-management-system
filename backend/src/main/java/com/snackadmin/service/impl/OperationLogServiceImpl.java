package com.snackadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.PageQuery;
import com.snackadmin.entity.SysOperationLog;
import com.snackadmin.mapper.SysOperationLogMapper;
import com.snackadmin.service.OperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class OperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements OperationLogService {

    @Override
    @Async
    public void saveLog(SysOperationLog log) {
        save(log);
    }

    @Override
    public Page<SysOperationLog> queryPage(PageQuery pageQuery, String module,
                                            String result, String startDate, String endDate) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(module), SysOperationLog::getModule, module)
                .eq(StringUtils.hasText(result), SysOperationLog::getResult, result)
                .orderByDesc(SysOperationLog::getCreatedAt);
        if (StringUtils.hasText(startDate))
            wrapper.ge(SysOperationLog::getCreatedAt, LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN));
        if (StringUtils.hasText(endDate))
            wrapper.le(SysOperationLog::getCreatedAt, LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX));
        return page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);
    }
}
