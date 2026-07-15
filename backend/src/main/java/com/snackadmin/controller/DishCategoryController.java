package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageQuery;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.DishCategorySaveDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.DishCategoryService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.DishCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "菜品分类管理")
@RestController
@RequestMapping("/api/dish-categories")
@RequiredArgsConstructor
public class DishCategoryController {

    private final DishCategoryService dishCategoryService;
    private final SysUserService sysUserService;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<PageResult<DishCategoryVO>> list(@Valid PageQuery pageQuery,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String status) {
        Page<DishCategoryVO> page = dishCategoryService.queryPage(pageQuery, name, status);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    public Result<DishCategoryVO> detail(@PathVariable Long id) {
        return Result.ok(dishCategoryService.getDetail(id));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DishCategorySaveDTO dto, Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishCategoryService.create(dto, operatorId);
        return Result.ok();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DishCategorySaveDTO dto,
                                Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishCategoryService.update(id, dto, operatorId);
        return Result.ok();
    }

    @Operation(summary = "更新分类状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status,
                                      Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishCategoryService.updateStatus(id, status, operatorId);
        return Result.ok();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishCategoryService.delete(id);
        return Result.ok();
    }

    private Long getOperatorId(Principal principal) {
        var user = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return user != null ? user.getId() : null;
    }
}
