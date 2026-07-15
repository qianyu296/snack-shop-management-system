package com.snackadmin.controller;

import com.snackadmin.common.Result;
import com.snackadmin.dto.ChangePasswordRequest;
import com.snackadmin.dto.LoginRequest;
import com.snackadmin.dto.RegisterRequest;
import com.snackadmin.dto.UpdateProfileRequest;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.LoginVO;
import com.snackadmin.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Tag(name = "认证管理", description = "登录、注册、个人信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO vo = sysUserService.login(request);
        return Result.ok(vo);
    }

    @Operation(summary = "店长注册申请")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<UserVO> getUserInfo(Principal principal) {
        // 从 SecurityContext 获取当前用户ID
        Long userId = getCurrentUserId(principal);
        UserVO vo = sysUserService.getCurrentUserInfo(userId);
        return Result.ok(vo);
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Principal principal) {
        Long userId = getCurrentUserId(principal);
        sysUserService.updateProfile(userId, request);
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        Long userId = getCurrentUserId(principal);
        sysUserService.changePassword(userId, request);
        return Result.ok();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，退出由前端清除令牌即可
        return Result.ok();
    }

    private Long getCurrentUserId(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("未登录");
        }
        com.snackadmin.entity.SysUser user = sysUserService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.snackadmin.entity.SysUser>()
                        .eq(com.snackadmin.entity.SysUser::getUsername, principal.getName())
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }
}
