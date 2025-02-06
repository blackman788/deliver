package com.oszero.deliver.server.web.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oszero.deliver.server.model.entity.SysAuthentication;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author black788
 * @since 2025-01-20
 */
public interface SysAuthenticationService extends IService<SysAuthentication> {

    /**
     * 根据sender查询认证信息
     */
    SysAuthentication getAuthenticationBySender(String sender);

}
