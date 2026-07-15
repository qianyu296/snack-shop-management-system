package com.snackadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.snackadmin.common.PageQuery;
import com.snackadmin.dto.*;
import com.snackadmin.entity.SysUser;
import com.snackadmin.vo.LoginVO;
import com.snackadmin.vo.UserVO;

/**
 * 用户服务
 */
public interface SysUserService extends IService<SysUser> {

    LoginVO login(LoginRequest request);

    void register(RegisterRequest request);

    void audit(Long userId, AuditRequest request, Long operatorId);

    void updateStatus(Long userId, String status, Long operatorId);

    void resetPassword(Long userId, ResetPasswordRequest request, Long operatorId);

    UserVO getCurrentUserInfo(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    Page<com.snackadmin.vo.UserPageVO> queryUsers(PageQuery pageQuery, String username,
                                                  String realName, String phone, String status);
}
