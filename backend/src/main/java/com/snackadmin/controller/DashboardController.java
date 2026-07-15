package com.snackadmin.controller;

import com.snackadmin.common.Result;
import com.snackadmin.service.DashboardService;
import com.snackadmin.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据看板")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取首页概览")
    @GetMapping("/summary")
    public Result<DashboardVO> summary() {
        return Result.ok(dashboardService.getDashboard());
    }
}
