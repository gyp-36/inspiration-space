package com.is.inspirationspacecommon.util.generator;


/**
 * 分布式ID生成器
 */

public class SnowflakeIdGenerator {
    // 起始时间戳（2023-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1672531200000L;

    // 各部分位数
    private static final long SEQUENCE_BITS = 8; // 序列号位数
    private static final long MACHINE_BITS = 4;    // 机器ID位数
    private static final long DATACENTER_BITS = 4; // 数据中心位数

    // 最大值计算（位运算优化）
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_MACHINE = ~(-1L << MACHINE_BITS);
    private static final long MAX_DATACENTER = ~(-1L << DATACENTER_BITS);

    // 左移位数
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATACENTER_BITS;

    private final long machineId;     // 机器ID
    private final long datacenterId;  // 数据中心ID
    private long sequence = 0;       // 序列号
    private long lastTimestamp = -1; // 上次生成时间

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        // 参数校验
        if (datacenterId > MAX_DATACENTER || datacenterId < 0) {
            throw new IllegalArgumentException("数据中心ID范围错误: 0~" + MAX_DATACENTER);
        }
        if (machineId > MAX_MACHINE || machineId < 0) {
            throw new IllegalArgumentException("机器ID范围错误: 0~" + MAX_MACHINE);
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 处理时钟回拨（强制等待）
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("系统时钟回拨！拒绝生成ID");
        }

        // 同一毫秒内生成
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0; // 新毫秒重置序列号
        }

        lastTimestamp = currentTimestamp;

        // 组合各部分生成ID
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    // 阻塞直到下一毫秒
    private long waitNextMillis(long lastTimestamp) {
        long current = getCurrentTimestamp();
        while (current <= lastTimestamp) {
            current = getCurrentTimestamp();
        }
        return current;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}