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

package com.oszero.deliver.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oszero.deliver.admin.model.entity.MessageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 针对表【message_record(消息记录)】的数据库操作Mapper
 *
 * @author black788
 * @version 1.0.0
 */
@Mapper
public interface AdminMessageRecordMapper extends BaseMapper<MessageRecord> {

    /**
     * 获取指定时间内的 TOP size
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param size      大小
     * @return 消息记录
     */
    @Select(
            "select template_id, count(*) as value from message_record where create_time >= #{startTime} and create_time <= #{endTime} group by template_id order by value desc limit 0, #{size};"
    )
    List<MessageRecord> getTemplateInfo(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Integer size);

    /**
     * 获取指定时间内的 TOP size
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param size      大小
     * @return 消息记录
     */
    @Select(
            "select app_id, count(*) as value from message_record where create_time >= #{startTime} and create_time <= #{endTime} group by app_id order by value desc limit 0, #{size};"
    )
    List<MessageRecord> getAppInfo(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Integer size);

    /**
     * 获取指定时间内的 TOP size
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param size      大小
     * @return 消息记录
     */
    @Select(
            "select push_user, count(*) as value from message_record where create_time >= #{startTime} and create_time <= #{endTime} group by push_user order by value desc limit 0, #{size};"
    )
    List<MessageRecord> getPushUserInfo(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Integer size);
}




