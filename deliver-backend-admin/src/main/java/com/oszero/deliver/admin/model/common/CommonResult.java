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

package com.oszero.deliver.admin.model.common;

import com.oszero.deliver.admin.enums.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 通用响应
 *
 * @param <T> 泛型
 * @author black788
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class CommonResult<T> {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 成功响应数据
     */
    private T data;

    /**
     * 错误响应信息
     */
    private String errorMessage;

    /**
     * 成功响应无参数
     *
     * @param <T> 泛型
     * @return CommonResult
     */
    public static <T> CommonResult<T> success() {
        return new CommonResult<>(ResultEnum.SUCCESS.getCode(), null, null);
    }

    /**
     * 成功响应有参数
     *
     * @param <T>  泛型
     * @param data 数据
     * @return CommonResult
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(ResultEnum.SUCCESS.getCode(), data, null);
    }

    /**
     * 失败响应无参数
     *
     * @param <T> 泛型
     * @return CommonResult
     */
    public static <T> CommonResult<T> fail() {
        return new CommonResult<>(ResultEnum.ERROR.getCode(), null, ResultEnum.ERROR.getMessage());
    }

    /**
     * 失败响应枚举
     *
     * @param <T>        泛型
     * @param resultEnum 返回枚举
     * @return CommonResult
     */
    public static <T> CommonResult<T> fail(ResultEnum resultEnum) {
        return new CommonResult<>(resultEnum.getCode(), null, resultEnum.getMessage());
    }

    /**
     * 失败响应错误信息
     *
     * @param <T>          泛型
     * @param errorMessage 错误信息
     * @return CommonResult
     */
    public static <T> CommonResult<T> fail(String errorMessage) {
        return new CommonResult<>(ResultEnum.ERROR.getCode(), null, errorMessage);
    }
}
