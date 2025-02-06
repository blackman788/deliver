package com.xxl.sso.server.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * @author: Black788
 * @date: 2025/1/13 21:49
 */
public class RandomKit {

    private static long counter_1 = 99999L;
    private static long counter_2 = 10000L;
    private static long counter_3 = 1000000L;

    public static void main(String[] args) {
        System.out.println(randomStr());
    }

    public RandomKit() {
    }

    public static String smsAuthCode(int codeLen) {
        return smsAuthCode(codeLen, RandomKit.SMSAuthCodeType.Numbers);
    }

    public static String uniqueStr() {
        return uniqueStr(5, true);
    }

    public static String uniqueStr(boolean flag) {
        return uniqueStr(5, flag);
    }

    public static synchronized String uniqueStr(int len, boolean flag) {
        len = len > 10 ? 10 : len;
        long temp = 1L;

        for(int i = 0; i < len - 1; ++i) {
            temp *= 10L;
        }

        if (flag) {
            --counter_1;
            if (counter_1 < 10000L) {
                counter_1 = 99999L;
            }

            return String.valueOf(9999999999999L - System.currentTimeMillis()) + counter_1 + (long)((Math.random() * 9.0 + 1.0) * (double)temp);
        } else {
            ++counter_2;
            if (counter_2 > 99999L) {
                counter_2 = 10000L;
            }

            return String.valueOf(System.currentTimeMillis()) + counter_2 + (long)((Math.random() * 9.0 + 1.0) * (double)temp);
        }
    }

    public static String smsAuthCode(int codeLen, SMSAuthCodeType type) {
        String randomCode = "";
        String strTable = type == RandomKit.SMSAuthCodeType.Numbers ? "1234567890" : "1234567890abcdefghijkmnpqrstuvwxyz";
        int len = strTable.length();
        boolean bDone = true;

        do {
            randomCode = "";
            int count = 0;

            for(int i = 0; i < codeLen; ++i) {
                double dblR = Math.random() * (double)len;
                int intR = (int)Math.floor(dblR);
                char c = strTable.charAt(intR);
                if ('0' <= c && c <= '9') {
                    ++count;
                }

                randomCode = randomCode + strTable.charAt(intR);
            }

            if (count >= 2) {
                bDone = false;
            }
        } while(bDone);

        return randomCode.toUpperCase();
    }

    public static synchronized String orderId() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

        ++counter_3;
        return dateFormatter.format(now) + timeFormatter.format(now) + counter_3;
    }

    public static synchronized String next() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        return dateFormatter.format(now) + timeFormatter.format(now) + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public static int random(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

    public static String randomStr(String prefix) {
        return prefix + uniqueStr(true);
    }

    public static String randomStr() {
        return uniqueStr(true);
    }

    public static String uuid(String prefix) {
        return prefix + uuid();
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static enum SMSAuthCodeType {
        Numbers,
        CharAndNumbers;

        private SMSAuthCodeType() {
        }
    }

}
