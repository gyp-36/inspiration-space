package com.is.inspirationspacecommon.util.generator;

import java.util.concurrent.*;

/**
 * 通用ID生成器(长度适中)
 */


public class CommonIdGenerator {
    // 生成房间号（时间戳后6位 + 3-4位随机后缀）
    public static synchronized Long generateRoomId() {
        // 1. 获取当前时间戳后4位（毫秒级）
        long timestampSuffix = System.currentTimeMillis() % 10000;

        // 2. 生成3-4位随机后缀 (100-9999)
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 10000);

        return (timestampSuffix * 10000L) + randomSuffix;


    }

}