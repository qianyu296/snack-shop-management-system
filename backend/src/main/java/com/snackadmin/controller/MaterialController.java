package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageQuery;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.MaterialSaveDTO;
import com.snackadmin.dto.StockAdjustDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.MaterialService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.MaterialVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "原料库存管理")
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final SysUserService sysUserService;

    @Operation(summary = "原料列表")
    @GetMapping
    public Result<PageResult<MaterialVO>> list(@Valid PageQuery pageQuery,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Boolean lowStock) {
        Page<MaterialVO> page = materialService.queryPage(pageQuery, name, category, status, lowStock);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "原料详情")
    @GetMapping("/{id}")
    public Result<MaterialVO> detail(@PathVariable Long id) {
        return Result.ok(materialService.getDetail(id));
    }

    @Operation(summary = "新增原料")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody MaterialSaveDTO dto, Principal principal) {
        Long operatorId = getOperatorId(principal);
        materialService.create(dto, operatorId);
        return Result.ok();
    }

    @Operation(summary = "修改原料")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MaterialSaveDTO dto,
                                Principal principal) {
        Long operatorId = getOperatorId(principal);
        materialService.update(id, dto, operatorId);
        return Result.ok();
    }

    @Operation(summary = "更新原料状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status,
                                      Principal principal) {
        Long operatorId = getOperatorId(principal);
        materialService.updateStatus(id, status, operatorId);
        return Result.ok();
    }

    @Operation(summary = "删除原料")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return Result.ok();
    }

    private Long getOperatorId(Principal principal) {
        var user = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return user != null ? user.getId() : null;
    }
}
