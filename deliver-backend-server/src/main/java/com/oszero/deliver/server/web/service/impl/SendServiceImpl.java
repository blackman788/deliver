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

package com.oszero.deliver.server.web.service.impl;

import cn.hutool.json.JSONUtil;
import com.oszero.deliver.server.cache.manager.ServerCacheManager;
import com.oszero.deliver.server.constant.TraceIdConstant;
import com.oszero.deliver.server.enums.StatusEnum;
import com.oszero.deliver.server.exception.MessageException;
import com.oszero.deliver.server.model.dto.common.PushWayDto;
import com.oszero.deliver.server.model.dto.common.SendTaskDto;
import com.oszero.deliver.server.model.dto.request.SendRequestDto;
import com.oszero.deliver.server.model.entity.App;
import com.oszero.deliver.server.model.entity.Template;
import com.oszero.deliver.server.model.entity.TemplateApp;
import com.oszero.deliver.server.pretreatment.common.LinkContext;
import com.oszero.deliver.server.pretreatment.common.LinkHandler;
import com.oszero.deliver.server.util.*;
import com.oszero.deliver.server.web.service.SendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 发送消息操作 Service 实现
 *
 * @author oszero
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SendServiceImpl implements SendService {

    private final ServerCacheManager serverCacheManager;
    private final LinkHandler linkHandler;
    private final AesUtils aesUtils;

    @Override
    public String send(SendRequestDto sendRequestDto) {

        // 1.设置 sendTaskDto
        SendTaskDto sendTaskDto = new SendTaskDto();
        ThreadLocalUtils.setSendTaskDto(sendTaskDto);

        sendTaskDto.setTemplateId(sendRequestDto.getTemplateId());
        sendTaskDto.setParamMap(sendRequestDto.getParamMap());
        sendTaskDto.setTraceId(MdcUtils.get(TraceIdConstant.TRACE_ID));
        sendTaskDto.setClientIp(IpUtils.getClientIp());
        sendTaskDto.setUsers(sendRequestDto.getUsers());
        sendTaskDto.setRetried(0);
        sendTaskDto.setRetry(sendRequestDto.getRetry());

        MessageLinkTraceUtils.recordMessageLifecycleInfoLog(sendTaskDto, "服务端接收到接入方推送消息");

        // 2.通过 templateId 获取 template
        Long templateId = sendRequestDto.getTemplateId();

        Template template = serverCacheManager.getTemplate(templateId);
        if (Objects.isNull(template)) {
            throw new MessageException(sendTaskDto, "传入的模板 ID 非法，请输入正确的 templateId");
        }

        Integer pushRange = template.getPushRange();
        Integer usersType = template.getUsersType();
        PushWayDto pushWayDto = JSONUtil.toBean(template.getPushWays(), PushWayDto.class);
        Integer channelType = pushWayDto.getChannelType();
        String messageType = pushWayDto.getMessageType();

        sendTaskDto.setPushRange(pushRange);
        sendTaskDto.setUsersType(usersType);
        sendTaskDto.setChannelType(channelType);
        sendTaskDto.setMessageType(messageType);

        // 关闭状态直接返回
        if (StatusEnum.OFF.getStatus().equals(template.getTemplateStatus())) {
            throw new MessageException(sendTaskDto, "此模板已禁用，再次使用请启用此模板");
        }

        MessageLinkTraceUtils.recordMessageLifecycleInfoLog(sendTaskDto, "完成消息模板检测");

        // 3.通过 templateId 获取 appId
        TemplateApp templateApp = serverCacheManager.getTemplateApp(templateId);
        if (Objects.isNull(templateApp)) {
            throw new MessageException(sendTaskDto, "未获取到模板所关联的应用，请检查关联的应用是否存在");
        }

        Long appId = templateApp.getAppId();
        sendTaskDto.setAppId(appId);

        // 4.得到 appConfig
        App app = serverCacheManager.getApp(appId);
        if (Objects.isNull(app)) {
            throw new MessageException(sendTaskDto, "未获取到模板所关联的应用，请检查关联的应用是否存在");
        }
        // 关闭状态直接返回
        if (StatusEnum.OFF.getStatus().equals(app.getAppStatus())) {
            throw new MessageException(sendTaskDto, "模板关联的应用为禁用状态，再次使用请启用");
        }

        sendTaskDto.setAppConfig(aesUtils.decrypt(app.getAppConfig()));

        MessageLinkTraceUtils.recordMessageLifecycleInfoLog(sendTaskDto, "完成消息模板关联应用检测");

        // 5.处理相关责任链
        LinkContext<SendTaskDto> context = LinkContext.<SendTaskDto>builder()
                .processModel(sendTaskDto)
                .code(usersType + "-" + channelType).build();
        linkHandler.process(context);

        // 6.返回 TraceId
        return sendTaskDto.getTraceId();
    }
}
