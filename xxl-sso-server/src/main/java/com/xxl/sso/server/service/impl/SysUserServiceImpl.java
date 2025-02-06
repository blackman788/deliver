package com.xxl.sso.server.service.impl;

import com.xxl.sso.server.entity.SysUser;
import com.xxl.sso.server.mapper.SysUserMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxl.sso.server.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author black788
 * @since 2025-01-12
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    public static final int DEFAULT_BATCH_SIZE = 1000;

}
