package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.PurchaseOrderQueryDTO;
import com.snackadmin.dto.PurchaseOrderSaveDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.PurchaseOrderService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.PurchaseOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "采购管理")
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final SysUserService sysUserService;

    @Operation(summary = "采购单列表")
    @GetMapping
    public Result<PageResult<PurchaseOrderVO>> list(@Valid PurchaseOrderQueryDTO query) {
        Page<PurchaseOrderVO> page = purchaseOrderService.queryPage(query);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "采购单详情")
    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> detail(@PathVariable Long id) {
        return Result.ok(purchaseOrderService.getDetail(id));
    }

    @Operation(summary = "创建采购单")
    @PostMapping
    public Result<PurchaseOrderVO> create(@Valid @RequestBody PurchaseOrderSaveDTO dto, Principal principal) {
        PurchaseOrderVO vo = purchaseOrderService.create(dto, getOperatorId(principal));
        return Result.ok("创建成功", vo);
    }

    @Operation(summary = "修改采购单（仅草稿）")
    @PutMapping("/{id}")
    public Result<PurchaseOrderVO> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderSaveDTO dto,
                                           Principal principal) {
        PurchaseOrderVO vo = purchaseOrderService.update(id, dto, getOperatorId(principal));
        return Result.ok("修改成功", vo);
    }

    @Operation(summary = "采购入库")
    @PostMapping("/{id}/warehouse")
    public Result<Void> warehouse(@PathVariable Long id, Principal principal) {
        purchaseOrderService.warehouse(id, getOperatorId(principal));
        return Result.ok();
    }

    @Operation(summary = "作废采购单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String reason, Principal principal) {
        purchaseOrderService.cancel(id, reason, getOperatorId(principal));
        return Result.ok();
    }

    private Long getOperatorId(Principal principal) {
        var u = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return u != null ? u.getId() : null;
    }
}
