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

package com.oszero.deliver.server.handler.impl;

import cn.hutool.json.JSONUtil;
import com.oszero.deliver.server.cache.manager.ServerCacheManager;
import com.oszero.deliver.server.client.feishu.FeiShuClient;
import com.oszero.deliver.server.enums.ChannelTypeEnum;
import com.oszero.deliver.server.handler.BaseHandler;
import com.oszero.deliver.server.model.app.FeiShuApp;
import com.oszero.deliver.server.model.dto.common.SendTaskDto;
import com.oszero.deliver.server.util.Resilience4jHelper;
import com.oszero.deliver.server.web.service.MessageRecordService;
import org.springframework.stereotype.Component;
import com.oszero.deliver.server.exception.MessageException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 飞书消费者处理器
 *
 * @author oszero
 * @version 1.0.0
 */
@Component
public class FeiShuHandler extends BaseHandler {

    private final FeiShuClient feiShuClient;
    private final ServerCacheManager serverCacheManager;
    private final Resilience4jHelper resilience4jHelper;

    /**
     * 可以批量发送的消息类型
     */
    private static final Set<String> BATCH_MESSAGE_TYPE =
            new HashSet<>(Arrays.asList("text", "image", "post", "share_chat", "interactive"));

    public FeiShuHandler(FeiShuClient feiShuClient, ServerCacheManager serverCacheManager, MessageRecordService messageRecordService, Resilience4jHelper resilience4jHelper) {
        this.feiShuClient = feiShuClient;
        this.serverCacheManager = serverCacheManager;
        this.messageRecordService = messageRecordService;
        this.resilience4jHelper = resilience4jHelper;
    }

    @Override
    protected void handle(SendTaskDto sendTaskDto) {
        String appConfigJson = sendTaskDto.getAppConfig();
        FeiShuApp feiShuApp = JSONUtil.toBean(appConfigJson, FeiShuApp.class);
        String tenantAccessToken = serverCacheManager.getFeiShuToken(feiShuApp, sendTaskDto);
        Map<String, Object> paramMap = sendTaskDto.getParamMap();
        String feiShuUserIdType = paramMap.get("feiShuUserIdType").toString();
        String msgType = paramMap.get("msg_type").toString();

        // 移除掉用户判断的 feiShuUserIdType
        paramMap.remove("feiShuUserIdType");

        // 添加Resilience4j保护
        resilience4jHelper.executeWithResilience(
            ChannelTypeEnum.FEI_SHU,
            () -> {
                try {
                    // 支持发送多种飞书的 usersId
                    if ("user_id".equals(feiShuUserIdType)) {
                        if (BATCH_MESSAGE_TYPE.contains(msgType)) {
                            feiShuClient.sendMessageBatch(tenantAccessToken, sendTaskDto);
                        } else {
                            feiShuClient.sendMessage(tenantAccessToken, sendTaskDto, feiShuUserIdType);
                        }
                    } else if ("email".equals(feiShuUserIdType)) {
                        feiShuClient.sendMessage(tenantAccessToken, sendTaskDto, feiShuUserIdType);
                    } else if ("chat_id".equals((feiShuUserIdType))) {
                        feiShuClient.sendMessage(tenantAccessToken, sendTaskDto, feiShuUserIdType);
                    } else if ("department_id".equals((feiShuUserIdType))) {
                        Object user_ids = paramMap.get("user_ids");
                        paramMap.remove("user_ids");
                        paramMap.put("department_ids", user_ids);
                        feiShuClient.sendMessageBatch(tenantAccessToken, sendTaskDto);
                    }
                    return null;
                } catch (Exception e) {
                    throw new MessageException(sendTaskDto, e.getMessage());
                }
            }
        );
    }
}
