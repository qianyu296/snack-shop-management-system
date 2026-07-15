package com.snackadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackadmin.common.PageQuery;
import com.snackadmin.common.Result;
import com.snackadmin.dto.AuditRequest;
import com.snackadmin.dto.ResetPasswordRequest;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.UserPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "用户管理", description = "账号审核、状态管理、密码重置")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @Operation(summary = "查询用户列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<com.snackadmin.common.PageResult<UserPageVO>> list(
            @Valid PageQuery pageQuery,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status) {
        Page<UserPageVO> page = sysUserService.queryUsers(pageQuery, username, realName, phone, status);
        return Result.ok(com.snackadmin.common.PageResult.of(
                page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize()));
    }

    @Operation(summary = "审核账号")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditRequest request, Principal principal) {
        Long operatorId = getCurrentUserId(principal);
        sysUserService.audit(id, request, operatorId);
        return Result.ok();
    }

    @Operation(summary = "更新账号状态（启用/禁用）")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status, Principal principal) {
        Long operatorId = getCurrentUserId(principal);
        sysUserService.updateStatus(id, status, operatorId);
        return Result.ok();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request,
                                       Principal principal) {
        Long operatorId = getCurrentUserId(principal);
        sysUserService.resetPassword(id, request, operatorId);
        return Result.ok();
    }

    private Long getCurrentUserId(Principal principal) {
        var user = sysUserService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.snackadmin.entity.SysUser>()
                        .eq(com.snackadmin.entity.SysUser::getUsername, principal.getName())
        );
        return user != null ? user.getId() : null;
    }
}
