package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.SupplierQueryDTO;
import com.snackadmin.dto.SupplierSaveDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.SupplierService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.SupplierVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "供应商管理")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final SysUserService sysUserService;

    @Operation(summary = "供应商列表")
    @GetMapping
    public Result<PageResult<SupplierVO>> list(@Valid SupplierQueryDTO query) {
        Page<SupplierVO> page = supplierService.queryPage(query);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "供应商详情")
    @GetMapping("/{id}")
    public Result<SupplierVO> detail(@PathVariable Long id) {
        return Result.ok(supplierService.getDetail(id));
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SupplierSaveDTO dto, Principal principal) {
        supplierService.create(dto, getOperatorId(principal));
        return Result.ok();
    }

    @Operation(summary = "修改供应商")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SupplierSaveDTO dto, Principal principal) {
        supplierService.update(id, dto, getOperatorId(principal));
        return Result.ok();
    }

    @Operation(summary = "更新供应商状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status, Principal principal) {
        supplierService.updateStatus(id, status, getOperatorId(principal));
        return Result.ok();
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.ok();
    }

    private Long getOperatorId(Principal principal) {
        var u = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return u != null ? u.getId() : null;
    }
}
