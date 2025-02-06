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

package com.oszero.deliver.server.util;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * AES 对称加密工具类
 *
 * @author oszero
 * @version 1.0.0
 */
@Slf4j
@Component
public class AesUtils {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_ALGORITHM_MODE = "AES/ECB/PKCS5Padding";

    /**
     * 秘钥
     */
    @Value("${aes.key}")
    private String aesKey;

    /**
     * 加密
     *
     * @param content 文本
     * @return 密文
     */
    public static String encrypt(String data, String key)  {
        String type = "AES";               //AES DES TRIPLEDES
        String mode = "ECB";               //mode ECB CBC CFB OFB CTR
        String pad = "PKCS5Padding";       //padding PKCS5Padding ISO10126Padding NoPadding
        try{
            Cipher cipher = Cipher.getInstance(type + "/" + mode + "/" + pad);
            byte[] keyBytes = key.getBytes();
            SecretKeySpec secret = new SecretKeySpec(keyBytes, type);
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            byte[] bytes = cipher.doFinal(data.getBytes());
            return Hex.encodeHexString(bytes);
        }catch (Exception e){
            log.error("AES加密失败", e);
            return data;
        }

    }

    /**
     * AES解密
     * 
     * @param data 待解密数据
     * @param key 密钥
     * @return 解密后的明文
     */
    public static String decrypt(String data, String key)  {
        String type = "AES";               //AES DES TRIPLEDES
        String mode = "ECB";               //mode ECB CBC CFB OFB CTR
        String pad = "PKCS5Padding";       //padding PKCS5Padding ISO10126Padding NoPadding

        try{
            Cipher cipher = Cipher.getInstance(type + "/" + mode + "/" + pad);
            byte[] keyBytes = key.getBytes();
            SecretKeySpec secret = new SecretKeySpec(keyBytes, type);
            cipher.init(Cipher.DECRYPT_MODE, secret);
            byte[] decrypt = cipher.doFinal(Hex.decodeHex(data));
            return new String(decrypt);
        }catch (Exception e){
            log.error("AES解密失败", e);
            return data;
        }
    }

    /**
     * 加密秘钥长度调整
     *
     * @param key           秘钥
     * @param desiredLength 长度 128、192、256
     * @return 调整后秘钥字节数组
     */
    public byte[] adjustKeyLength(byte[] key, int desiredLength) {
        if (key.length == desiredLength) {
            return key; // 密钥长度已符合要求
        }
        // 密钥长度不符，调整密钥长度
        return Arrays.copyOf(key, desiredLength);
    }

    /**
     * 加密
     *
     * @param content 文本
     * @return 密文
     */
    public String encrypt(String content) {
        byte[] key = adjustKeyLength(aesKey.getBytes(StandardCharsets.UTF_8), 24);
        AES aes = new AES(key);
        //构建
        return aes.encryptHex(content);
    }

    /**
     * 解密
     *
     * @param encrypt 密文
     * @return 文本
     */
    public String decrypt(String encrypt) {
        byte[] key = adjustKeyLength(aesKey.getBytes(StandardCharsets.UTF_8), 24);
        AES aes = new AES(key);
        //构建
        return aes.decryptStr(encrypt);
    }


    public static void main(String[] args) throws Exception {
        String aes = "Fhx&663rKM$hA!N4";
        System.out.println(AesUtils.decrypt("9a0adcd7c9c3b72e532bef13cced0997", aes));
        System.out.println(AesUtils.encrypt("123456", aes));
    }
}
