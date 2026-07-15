package com.snackadmin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.snackadmin.common.BusinessException;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.*;
import com.snackadmin.entity.SysRole;
import com.snackadmin.entity.SysUser;
import com.snackadmin.entity.SysUserRole;
import com.snackadmin.enums.RoleCode;
import com.snackadmin.enums.UserStatus;
import com.snackadmin.mapper.SysRoleMapper;
import com.snackadmin.mapper.SysUserMapper;
import com.snackadmin.mapper.SysUserRoleMapper;
import com.snackadmin.security.JwtTokenProvider;
import com.snackadmin.service.SysUserService;
import com.snackadmin.vo.LoginVO;
import com.snackadmin.vo.UserPageVO;
import com.snackadmin.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public LoginVO login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, request.getUsername()));
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 生成 JWT
            String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(),
                    Map.of("roles", getUserRoles(user.getId())));

            // 查询角色信息
            List<String> roles = getUserRoles(user.getId());

            return LoginVO.builder()
                    .token(token)
                    .id(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .roles(roles)
                    .permissions(roles) // 简化：权限同角色
                    .build();
        } catch (BadCredentialsException e) {
            throw new BusinessException("用户名或密码错误");
        }
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 校验确认密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 校验用户名唯一
        if (exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()))) {
            throw new BusinessException(409, "用户名已存在");
        }

        // 校验手机号唯一
        if (exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, request.getPhone()))) {
            throw new BusinessException(409, "手机号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.PENDING);
        save(user);

        // 默认分配店长角色
        SysRole shopManagerRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, RoleCode.SHOP_MANAGER)
        );
        if (shopManagerRole != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(shopManagerRole.getId());
            sysUserRoleMapper.insert(userRole);
        }
    }

    @Override
    @Transactional
    public void audit(Long userId, AuditRequest request, Long operatorId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException(409, "只能审核待审核状态的账号");
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            user.setStatus(UserStatus.ENABLED);
            user.setRejectReason(null);
        } else {
            if (!StringUtils.hasText(request.getRejectReason())) {
                throw new BusinessException("驳回时必须填写驳回原因");
            }
            user.setStatus(UserStatus.REJECTED);
            user.setRejectReason(request.getRejectReason());
        }
        user.setUpdatedBy(operatorId);
        updateById(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, String status, Long operatorId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserStatus targetStatus;
        try {
            targetStatus = UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的状态值: " + status);
        }

        if (targetStatus != UserStatus.ENABLED && targetStatus != UserStatus.DISABLED) {
            throw new BusinessException("只能启用或禁用账号");
        }

        // 管理员不能被禁用
        if (targetStatus == UserStatus.DISABLED) {
            List<String> roles = getUserRoles(userId);
            if (roles.contains(RoleCode.ADMIN.getCode())) {
                throw new BusinessException("不能禁用管理员账号");
            }
        }

        user.setStatus(targetStatus);
        user.setUpdatedBy(operatorId);
        updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request, Long operatorId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(operatorId);
        updateById(user);
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus().getCode());
        vo.setRoles(getUserRoles(userId));
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 校验手机号唯一（排除自己）
        if (exists(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, request.getPhone())
                .ne(SysUser::getId, userId))) {
            throw new BusinessException(409, "手机号已被使用");
        }

        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);
    }

    @Override
    public Page<UserPageVO> queryUsers(PageQuery pageQuery, String username,
                                        String realName, String phone, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .like(StringUtils.hasText(phone), SysUser::getPhone, phone)
                .eq(StringUtils.hasText(status), SysUser::getStatus,
                        StringUtils.hasText(status) ? UserStatus.valueOf(status) : null)
                .orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> page = page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);

        return (Page<UserPageVO>) page.convert(user -> {
            UserPageVO vo = new UserPageVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus().getCode());
            vo.setRejectReason(user.getRejectReason());
            vo.setCreatedAt(user.getCreatedAt());
            return vo;
        });
    }

    private List<String> getUserRoles(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .map(r -> r.getRoleCode().getCode())
                .toList();
    }
}
