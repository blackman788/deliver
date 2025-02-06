package com.oszero.deliver.server.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oszero.deliver.server.model.entity.ResilienceConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Resilience4j配置表 Mapper 接口
 * </p>
 *
 * @author black788
 * @since 2025-02-01
 */
public interface ResilienceConfigMapper extends BaseMapper<ResilienceConfig> {

    @Select("SELECT * FROM resilience_config WHERE channel_code = #{channelCode} AND config_type = #{configType}")
    List<ResilienceConfig> selectByChannelAndType(@Param("channelCode") Integer channelCode, 
                                                  @Param("configType") String configType);
}
