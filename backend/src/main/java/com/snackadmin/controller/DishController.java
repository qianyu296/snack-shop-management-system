package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageQuery;
import com.snackadmin.common.PageResult;
import com.snackadmin.common.Result;
import com.snackadmin.dto.DishSaveDTO;
import com.snackadmin.entity.SysUser;
import com.snackadmin.service.DishService;
import com.snackadmin.service.SysUserService;
import com.snackadmin.util.FileUploadUtil;
import com.snackadmin.vo.DishPageVO;
import com.snackadmin.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Tag(name = "菜品管理")
@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;
    private final SysUserService sysUserService;
    private final FileUploadUtil fileUploadUtil;

    @Operation(summary = "菜品列表")
    @GetMapping
    public Result<PageResult<DishPageVO>> list(@Valid PageQuery pageQuery,
                                                @RequestParam(required = false) String name,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String saleStatus,
                                                @RequestParam(required = false) String recommendStatus) {
        Page<DishPageVO> page = dishService.queryPage(pageQuery, name, categoryId, saleStatus, recommendStatus);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "菜品详情")
    @GetMapping("/{id}")
    public Result<DishVO> detail(@PathVariable Long id) {
        return Result.ok(dishService.getDetail(id));
    }

    @Operation(summary = "获取已上架菜品（供订单使用）")
    @GetMapping("/on-sale")
    public Result<List<DishVO>> onSaleDishes() {
        return Result.ok(dishService.getOnSaleDishes());
    }

    @Operation(summary = "新增菜品")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody DishSaveDTO dto, Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishService.create(dto, operatorId);
        return Result.ok();
    }

    @Operation(summary = "修改菜品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DishSaveDTO dto,
                                Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishService.update(id, dto, operatorId);
        return Result.ok();
    }

    @Operation(summary = "上架/下架菜品")
    @PutMapping("/{id}/sale-status")
    public Result<Void> updateSaleStatus(@PathVariable Long id, @RequestParam String saleStatus,
                                          Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishService.updateSaleStatus(id, saleStatus, operatorId);
        return Result.ok();
    }

    @Operation(summary = "设置/取消推荐")
    @PutMapping("/{id}/recommend-status")
    public Result<Void> updateRecommendStatus(@PathVariable Long id, @RequestParam String recommendStatus,
                                               Principal principal) {
        Long operatorId = getOperatorId(principal);
        dishService.updateRecommendStatus(id, recommendStatus, operatorId);
        return Result.ok();
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "上传菜品图片")
    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadUtil.uploadDishImage(file);
        return Result.ok(url);
    }

    private Long getOperatorId(Principal principal) {
        var user = sysUserService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, principal.getName()));
        return user != null ? user.getId() : null;
    }
}
