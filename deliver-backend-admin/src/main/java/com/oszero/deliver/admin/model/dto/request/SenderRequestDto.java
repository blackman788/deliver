package com.oszero.deliver.admin.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author: Black788
 * @date: 2025/1/20 0:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SenderRequestDto extends PageRequest{

    private String id;

    private String sender;

    private String password;

    private String encryption;

    private String secret;

    //@NotNull
    private Long groupId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
