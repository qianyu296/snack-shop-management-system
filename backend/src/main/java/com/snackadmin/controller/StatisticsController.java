package com.snackadmin.controller;

import com.snackadmin.common.Result;
import com.snackadmin.service.StatisticsService;
import com.snackadmin.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "经营统计")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取经营统计数据")
    @GetMapping("/business")
    public Result<StatisticsVO> business(@RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate) {
        return Result.ok(statisticsService.getStatistics(startDate, endDate));
    }
}
