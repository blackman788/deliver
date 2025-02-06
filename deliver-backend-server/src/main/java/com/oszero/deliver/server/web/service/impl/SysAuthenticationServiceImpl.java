package com.oszero.deliver.server.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.oszero.deliver.server.model.entity.SysAuthentication;
import com.oszero.deliver.server.web.mapper.SysAuthenticationMapper;
import com.oszero.deliver.server.web.service.SysAuthenticationService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author black788
 * @since 2025-01-20
 */
@Service
public class SysAuthenticationServiceImpl extends ServiceImpl<SysAuthenticationMapper, SysAuthentication> implements SysAuthenticationService {


    @Override
    public SysAuthentication getAuthenticationBySender(String sender) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<SysAuthentication>().eq(SysAuthentication::getSender, sender));
    }
}
