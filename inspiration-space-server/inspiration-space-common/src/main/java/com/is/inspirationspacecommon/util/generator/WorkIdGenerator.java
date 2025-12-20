package com.is.inspirationspacecommon.util.generator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作品ID生成器(8位数字)
 * 结构: [时间戳后5位][序列号(3位)]
 */
public class WorkIdGenerator {
    // 时间戳后5位
    private static final long TIME_MOD = 100000L;
    // 每毫秒最大序列号(0-999)
    private static final int MAX_SEQUENCE = 999;
    private static final AtomicInteger sequence = new AtomicInteger(0);
    private static long lastTimestamp = -1L;

    /**
     * 生成作品ID
     * @return 作品ID
     */
    public static synchronized long generateId() {
        long timestamp = System.currentTimeMillis() % TIME_MOD;

        if (timestamp == lastTimestamp) {
            // 同一毫秒内
            int currentSequence = sequence.incrementAndGet();
            if (currentSequence > MAX_SEQUENCE) {
                // 超过最大序列号，等待到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
                sequence.set(0);
            }
        } else {
            // 新的毫秒
            sequence.set(0);
        }

        lastTimestamp = timestamp;
        return timestamp * 1000L + sequence.get();
    }

    /**
     * 等待到下一毫秒
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() % TIME_MOD;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() % TIME_MOD;
        }
        return timestamp;
    }


}