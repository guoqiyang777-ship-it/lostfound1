package com.example.lostfound.service;

import com.example.lostfound.pojo.Admin;
import com.example.lostfound.pojo.dto.AdminLoginDTO;
import com.example.lostfound.pojo.vo.Result;

/**
 * 管理员服务接口
 */
public interface AdminService {
    /**
     * 管理员登录
     *
     * @param loginDTO 登录信息
     * @return 结果
     */
    Result<String> login(AdminLoginDTO loginDTO);

    /**
     * 获取管理员信息
     *
     * @param adminId 管理员ID
     * @return 结果
     */
    Result<Admin> getAdminInfo(Long adminId);
}