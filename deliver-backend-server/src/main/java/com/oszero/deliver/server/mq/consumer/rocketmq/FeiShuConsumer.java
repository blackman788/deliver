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

package com.oszero.deliver.server.mq.consumer.rocketmq;

import com.oszero.deliver.server.constant.MQConstant;
import com.oszero.deliver.server.handler.impl.FeiShuHandler;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 飞书 RocketMQConsumer
 *
 * @author oszero
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MQConstant.FEI_SHU_TOPIC, consumerGroup = MQConstant.FEI_SHU_CONSUMER_GROUP)
@ConditionalOnProperty(value = "mq-type", havingValue = "rocketmq")
public class FeiShuConsumer implements RocketMQListener<MessageExt> {

    private final FeiShuHandler feiShuHandler;
    private final RocketMQCommonConsumer rocketMQCommonConsumer;

    /**
     * 没有报错，就签收
     * 如果报错，就是拒收 就会重试
     *
     * @param messageExt 消息对象
     */
    @Override
    public void onMessage(MessageExt messageExt) {
        rocketMQCommonConsumer.omMessageAck(messageExt, feiShuHandler);
    }
}
