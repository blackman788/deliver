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

package com.oszero.deliver.admin.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oszero.deliver.admin.model.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息模板
 *
 * @author black788
 * @version 1.0.0
 */
@Data
@TableName(value = "template")
@EqualsAndHashCode(callSuper = true)
public class Template extends BaseEntity implements Serializable {
    /**
     * 模板id
     */
    @TableId(type = IdType.AUTO)
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 推送范围（0-不限 1-企业内部 2-外部）
     */
    private Integer pushRange;

    /**
     * 用户类型（1-企业账号 2-电话 3-平台ID 4-邮箱）
     */
    private Integer usersType;

    /**
     * 推送方式
     * {
     * "channelType":（1-打电话 2-发短信 3-邮件 4-钉钉 5-企业微信 6-飞书）
     * "messageType": 所见 MessageTypeEnum
     * }
     */
    private String pushWays;

    /**
     * 模板使用数
     */
    private Integer useCount;

    /**
     * 模板状态
     */
    private Integer templateStatus;

    /**
     * groupId
     */
    @TableField(fill = FieldFill.INSERT)
    private Long groupId = -1L;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-不删除 1-删除
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}