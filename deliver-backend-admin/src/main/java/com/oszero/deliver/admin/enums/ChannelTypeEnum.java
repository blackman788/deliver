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

package com.oszero.deliver.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 渠道类型枚举
 *
 * @author black788
 * @version 1.0.0
 */
@Getter
@ToString
@AllArgsConstructor
public enum ChannelTypeEnum {

    CALL(1, "CALL", "电话"),
    SMS(2, "SMS", "短信"),
    MAIL(3, "MAIL", "邮件"),
    DING(4, "DING", "钉钉"),
    WECHAT(5, "WECHAT", "企微"),
    FEI_SHU(6, "FEISHU", "飞书");

    private final Integer code;    // 编号
    private final String name;      // 名称
    private final String desc;      // 中文名称

    /**
     * 通过 code 获取实例
     *
     * @param code 编号
     * @return 实例
     */
    public static ChannelTypeEnum getInstanceByCode(Integer code) {
        for (ChannelTypeEnum v : values()) {
            if (v.getCode().equals(code)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 通过 code 获取实例
     *
     * @param code 编码
     * @return 实例
     */
    public static ChannelTypeEnum getInstanceByName(String name) {
        for (ChannelTypeEnum v : values()) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 通过 name 获取实例
     *
     * @param name 名称
     * @return 实例
     */
    public static ChannelTypeEnum getInstanceByDesc(String desc) {
        for (ChannelTypeEnum v : values()) {
            if (v.getDesc().equals(desc)) {
                return v;
            }
        }
        return null;
    }
}
