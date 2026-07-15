package com.snackadmin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackadmin.entity.SysRole;
import com.snackadmin.entity.SysUser;
import com.snackadmin.entity.SysUserRole;
import com.snackadmin.enums.UserStatus;
import com.snackadmin.mapper.SysRoleMapper;
import com.snackadmin.mapper.SysUserMapper;
import com.snackadmin.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security UserDetailsService 实现
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new UsernameNotFoundException("账号待审核，请等待管理员审核通过");
        }
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new UsernameNotFoundException("账号申请已被驳回");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UsernameNotFoundException("账号已被禁用");
        }

        // 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );

        List<SimpleGrantedAuthority> authorities;
        if (userRoles.isEmpty()) {
            authorities = Collections.emptyList();
        } else {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
            List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode().getCode()))
                    .toList();
        }

        return new User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == UserStatus.ENABLED,
                true, true, true,
                authorities
        );
    }
}
