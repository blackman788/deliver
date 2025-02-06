package com.oszero.deliver.admin.model.dto.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: Black788
 * @date: 2025/1/20 0:26
 */
@Data
public class SenderResponseDto {

    @TableId(value = "id", type = IdType.NONE)
    private String id;

    private String sender;

    private String password;

    private String encryption;

    private String secret;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
