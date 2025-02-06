/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oszero.deliver.server.web.controller;

import cn.hutool.crypto.digest.MD5;
import com.oszero.deliver.server.common.CommonResult;
import com.oszero.deliver.server.config.resilience4j.ResilienceConfigManager;
import com.oszero.deliver.server.model.dto.request.SendRequestDto;
import com.oszero.deliver.server.model.entity.SysAuthentication;
import com.oszero.deliver.server.web.service.SendService;
import com.oszero.deliver.server.web.service.SysAuthenticationService;

import com.oszero.deliver.server.util.AesUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 开放接口
 *
 * @author oszero
 * @version 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {

    private final SendService sendService;

    private final ResilienceConfigManager resilienceConfigManager;

    private final SysAuthenticationService sysAuthenticationService;

    /**
     * 发送消息
     *
     * @param sendRequestDto 发送请求 DTO
     * @return 返回成功信息
     */
    @PostMapping("/sendMessage")
    public CommonResult<String> sendMessage(@Valid @RequestBody SendRequestDto sendRequestDto) throws Exception {
        // 根据sender查询认证信息
        SysAuthentication authentication = sysAuthenticationService.getAuthenticationBySender(sendRequestDto.getSender());
        if (authentication == null) {
            return CommonResult.fail("无效的发送方");
        }
        
        // 验证密码
        String password = sendRequestDto.getPassword();
        String storedPassword = authentication.getPassword();
        String secretKey = authentication.getSecret();
        String encryption = authentication.getEncryption();
        
        boolean passwordMatch;
        switch (encryption) {
            case "0": // 不加密
                passwordMatch = password.equals(storedPassword);
                break;
            case "1": // AES加密
                passwordMatch = AesUtils.decrypt(password, secretKey).equals(storedPassword);
                break;
            case "2": // MD5加密
                passwordMatch = MD5.create().digestHex(password).equals(storedPassword);
                break;
            default:
                return CommonResult.fail("不支持的加密类型");
        }
        
        if (!passwordMatch) {
            return CommonResult.fail("无效的密码");
        }
        
        // 身份验证通过,继续处理发送请求
        String traceId = sendService.send(sendRequestDto);
        return CommonResult.success(traceId);
    }

    @PostMapping("/resilience/config/{code}")
    public CommonResult<String> updateResilienceConfig(@PathVariable Integer code) {
        try {
            resilienceConfigManager.updateResilienceConfig(code);
            log.info("Resilience config updated successfully code: "+ code);
            return CommonResult.success("Resilience config updated successfully");
        } catch (Exception e) {
            log.error("Failed to update resilience config", e);
            return CommonResult.fail("Failed to update config");
        }
    }

}
