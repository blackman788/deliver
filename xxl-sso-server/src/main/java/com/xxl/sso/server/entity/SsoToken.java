package com.xxl.sso.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: Black788
 * @date: 2025/1/18 17:21
 */
@Data
public class SsoToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;

    private String redirectUrl;

}
