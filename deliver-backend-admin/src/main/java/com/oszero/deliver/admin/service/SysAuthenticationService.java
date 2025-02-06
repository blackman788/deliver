package com.oszero.deliver.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oszero.deliver.admin.model.dto.request.SenderRequestDto;
import com.oszero.deliver.admin.model.dto.response.SenderResponseDto;
import com.oszero.deliver.admin.model.entity.SysAuthentication;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author black788
 * @since 2025-01-20
 */
public interface SysAuthenticationService extends IService<SysAuthentication> {

    Page<SenderResponseDto> getSenderList(SenderRequestDto dto);
}
