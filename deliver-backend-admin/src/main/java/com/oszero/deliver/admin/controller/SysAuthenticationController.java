package com.oszero.deliver.admin.controller;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oszero.deliver.admin.model.common.CommonResult;
import com.oszero.deliver.admin.model.dto.request.SenderRequestDto;
import com.oszero.deliver.admin.model.dto.response.SenderResponseDto;
import com.oszero.deliver.admin.model.entity.SysAuthentication;
import com.oszero.deliver.admin.service.SysAuthenticationService;
import com.oszero.deliver.admin.util.RandomKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author black788
 * @since 2025-01-20
 */
@RestController
@RequestMapping("/receiver")
public class SysAuthenticationController {

    @Autowired
    private SysAuthenticationService sysAuthenticationService;


    @PostMapping("/getPageReceiver")
    public CommonResult<Page<SenderResponseDto>> getSender(@RequestBody SenderRequestDto dto) {
        Page<SenderResponseDto> senderRequestDtoPage = sysAuthenticationService.getSenderList(dto);
        return CommonResult.success(senderRequestDtoPage);
    }

    @PostMapping("/saveReceiver")
    public CommonResult<?> saveReceiver(@RequestBody SenderRequestDto dto) {
        SysAuthentication  sysAuthentication = new SysAuthentication();
        BeanUtils.copyProperties(dto,sysAuthentication);
        sysAuthentication.setId(RandomKit.randomStr());
        sysAuthentication.setCreateTime(DateUtil.now());
        sysAuthenticationService.save(sysAuthentication);
        return CommonResult.success();
    }

    @PostMapping("/updateReceiver")
    public CommonResult<?> updateReceiver(@RequestBody SenderRequestDto dto) {
        SysAuthentication  sysAuthentication = new SysAuthentication();
        BeanUtils.copyProperties(dto,sysAuthentication);
        sysAuthentication.setUpdateTime(DateUtil.now());
        sysAuthenticationService.updateById(sysAuthentication);
        return CommonResult.success();
    }

    @PostMapping("/deleteReceiver")
    public CommonResult<?> deleteReceiver(@RequestBody SenderRequestDto dto) {
        sysAuthenticationService.removeById(dto.getId());
        return CommonResult.success();
    }



}
