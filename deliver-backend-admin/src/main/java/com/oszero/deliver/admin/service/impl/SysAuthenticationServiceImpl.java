package com.oszero.deliver.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.oszero.deliver.admin.model.dto.request.SenderRequestDto;

import com.oszero.deliver.admin.model.dto.response.SenderResponseDto;

import com.oszero.deliver.admin.model.entity.SysAuthentication;
import com.oszero.deliver.admin.mapper.SysAuthenticationMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oszero.deliver.admin.service.SysAuthenticationService;
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
    public Page<SenderResponseDto> getSenderList(SenderRequestDto dto) {
        if (Objects.isNull(dto.getGroupId())) {
            dto.setGroupId(-1L);
        }
        LambdaQueryWrapper<SysAuthentication> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(dto.getSender()), SysAuthentication::getSender, dto.getSender())
                .eq(!Objects.isNull(dto.getEncryption()), SysAuthentication::getEncryption, dto.getEncryption())
                .ge(!Objects.isNull(dto.getCreateTime()), SysAuthentication::getCreateTime, dto.getCreateTime())
                .le(!Objects.isNull(dto.getCreateTime()), SysAuthentication::getCreateTime, dto.getCreateTime())
                .orderByDesc(SysAuthentication::getCreateTime);
        Page<SysAuthentication> senderPage = new Page<>(dto.getCurrentPage(), dto.getPageSize());

        this.page(senderPage, wrapper);

        Page<SenderResponseDto> senderResponseDtoPage = new Page<>(senderPage.getPages(), senderPage.getSize());
        senderResponseDtoPage.setRecords(senderPage.getRecords().stream().map(platformFile -> {
            SenderResponseDto senderResponseDto = new SenderResponseDto();
            BeanUtil.copyProperties(platformFile, senderResponseDto);

            return senderResponseDto;
        }).collect(Collectors.toList()));

        senderPage.setTotal(senderPage.getTotal());

        return senderResponseDtoPage;
    }
}
