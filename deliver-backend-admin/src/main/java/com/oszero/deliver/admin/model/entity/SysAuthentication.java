package com.oszero.deliver.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author black788
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_authentication")
public class SysAuthentication implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String sender;

    private String password;

    private String encryption;

    private String secret;

    private String createTime;

    private String updateTime;


}
