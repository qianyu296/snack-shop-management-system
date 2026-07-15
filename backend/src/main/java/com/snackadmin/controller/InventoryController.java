package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.InventoryRecordQueryDTO;
import com.snackadmin.dto.StockAdjustDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.InventoryRecordService;
import com.snackadmin.service.MaterialService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.InventoryRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "库存管理")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRecordService inventoryRecordService;
    private final MaterialService materialService;
    private final SysUserService sysUserService;

    @Operation(summary = "库存流水查询")
    @GetMapping("/records")
    public Result<PageResult<InventoryRecordVO>> records(@Valid InventoryRecordQueryDTO query) {
        Page<InventoryRecordVO> page = inventoryRecordService.queryPage(query);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "库存调整")
    @PostMapping("/adjustments")
    public Result<Void> adjustStock(@Valid @RequestBody StockAdjustDTO dto, Principal principal) {
        Long operatorId = getOperatorId(principal);
        materialService.adjustStock(dto, operatorId);
        return Result.ok();
    }

    private Long getOperatorId(Principal principal) {
        var user = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return user != null ? user.getId() : null;
    }
}
