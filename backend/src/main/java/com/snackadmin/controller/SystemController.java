package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageQuery;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.entity.SysOperationLog;
import com.snackadmin.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统管理")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemController {

    private final OperationLogService operationLogService;

    @Operation(summary = "操作日志查询")
    @GetMapping("/logs")
    public Result<PageResult<SysOperationLog>> logs(@Valid PageQuery pageQuery,
                                                     @RequestParam(required = false) String module,
                                                     @RequestParam(required = false) String result,
                                                     @RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        Page<SysOperationLog> page = operationLogService.queryPage(pageQuery, module, result, startDate, endDate);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }
}
