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

package com.oszero.deliver.admin.controller;

import com.oszero.deliver.admin.model.common.CommonResult;
import com.oszero.deliver.admin.model.dto.request.DashboardDateSelectRequestDto;
import com.oszero.deliver.admin.model.dto.response.DashboardHeadDataResponseDto;
import com.oszero.deliver.admin.model.dto.response.DashboardInfoResponseDto;
import com.oszero.deliver.admin.model.dto.response.MessageInfoResponseReactDto;
import com.oszero.deliver.admin.model.dto.response.MessageInfoResponseVueDto;
import com.oszero.deliver.admin.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘
 *
 * @author black788
 * @version 1.0.0
 */
@Validated
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取数据面板头数据
     *
     * @return 头数据
     */
    @PostMapping("/getDashboardHeadData")
    public CommonResult<DashboardHeadDataResponseDto> getDashboardHeadData() {
        DashboardHeadDataResponseDto dto = dashboardService.getDashboardHeadData();
        return CommonResult.success(dto);
    }

    /**
     * 获取消息详情 vue 版本
     *
     * @param dto 日期选择
     * @return 消息详情
     */
    @PostMapping("/getMessageInfo/vue")
    public CommonResult<MessageInfoResponseVueDto> getMessageInfoVue(@Valid @RequestBody DashboardDateSelectRequestDto dto) {
        MessageInfoResponseVueDto messageInfoResponseVueDto = dashboardService.getMessageInfoVue(dto);
        return CommonResult.success(messageInfoResponseVueDto);
    }

    /**
     * 获取消息详情 React 版本
     *
     * @param dto 日期选择
     * @return 消息详情
     */
    @PostMapping("/getMessageInfo/react")
    public CommonResult<List<MessageInfoResponseReactDto>> getMessageInfoReact(@Valid @RequestBody DashboardDateSelectRequestDto dto) {
        List<MessageInfoResponseReactDto> messageInfoResponseReactDtoList = dashboardService.getMessageInfoReact(dto);
        return CommonResult.success(messageInfoResponseReactDtoList);
    }

    /**
     * 获取模版详情
     *
     * @param dto 日期选择
     * @return 模版详情
     */
    @PostMapping("/getTemplateInfo")
    public CommonResult<DashboardInfoResponseDto> getTemplateInfo(@Valid @RequestBody DashboardDateSelectRequestDto dto) {
        DashboardInfoResponseDto dashboardInfoResponseDto = dashboardService.getTemplateInfo(dto);
        return CommonResult.success(dashboardInfoResponseDto);
    }

    /**
     * 获取 APP 详情
     *
     * @param dto 日期选择
     * @return APP 详情
     */
    @PostMapping("/getAppInfo")
    public CommonResult<DashboardInfoResponseDto> getAppInfo(@Valid @RequestBody DashboardDateSelectRequestDto dto) {
        DashboardInfoResponseDto appInfoResponseDto = dashboardService.getAppInfo(dto);
        return CommonResult.success(appInfoResponseDto);
    }

    /**
     * 获取推送用户详情
     *
     * @param dto 日期选择
     * @return 推送用户详情
     */
    @PostMapping("/getPushUserInfo")
    public CommonResult<DashboardInfoResponseDto> getPushUserInfo(@Valid @RequestBody DashboardDateSelectRequestDto dto) {
        DashboardInfoResponseDto pushUserInfoResponseDto = dashboardService.getPushUserInfo(dto);
        return CommonResult.success(pushUserInfoResponseDto);
    }
}
