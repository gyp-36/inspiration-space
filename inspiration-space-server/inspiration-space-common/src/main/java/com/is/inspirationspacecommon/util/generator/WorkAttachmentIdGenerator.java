package com.is.inspirationspacecommon.util.generator;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作品附件ID生成器(10位数字)
 * 结构: [作品ID(8位)][附件序列号(2位)]
 */
public class WorkAttachmentIdGenerator {

    // 每个作品的最大附件数(0-99)
    private static final int MAX_ATTACHMENT_PER_WORK = 100;

    // 存储每个作品的附件序列号
    private static final ConcurrentHashMap<Long, AtomicInteger> workSequences = new ConcurrentHashMap<>();

    /**
     * 为指定作品生成附件ID
     * @param workId 作品ID
     * @return 附件ID (10位数字)
     * @throws IllegalStateException 如果超过最大附件数限制
     */
    public static long generateAttachmentId(long workId) {
        // 获取或创建该作品的序列号生成器
        AtomicInteger sequence = workSequences.computeIfAbsent(workId, k -> new AtomicInteger(0));

        int currentSequence = sequence.getAndIncrement();

        if (currentSequence >= MAX_ATTACHMENT_PER_WORK) {
            throw new IllegalStateException("作品 " + workId + " 的附件数量已超过最大限制(" + MAX_ATTACHMENT_PER_WORK + ")");
        }

        // 附件ID = 作品ID * 100 + 序列号
        return workId * 100L + currentSequence;
    }

    /**
     * 获取指定作品的当前附件数量
     * @param workId 作品ID
     * @return 当前附件数量
     */
    public static int getCurrentAttachmentCount(long workId) {
        AtomicInteger sequence = workSequences.get(workId);
        return sequence != null ? sequence.get() : 0;
    }

    /**
     * 重置指定作品的附件序列号（谨慎使用）
     * @param workId 作品ID
     */
    public static void resetSequence(long workId) {
        workSequences.remove(workId);
    }

    /**
     * 清理所有作品的序列号数据（谨慎使用）
     */
    public static void clearAllSequences() {
        workSequences.clear();
    }

    /**
     * 从附件ID解析出作品ID
     * @param attachmentId 附件ID
     * @return 作品ID
     */
    public static long parseWorkId(long attachmentId) {
        return attachmentId / 100L;
    }

    /**
     * 从附件ID解析出附件序列号
     * @param attachmentId 附件ID
     * @return 附件序列号
     */
    public static int parseAttachmentSequence(long attachmentId) {
        return (int) (attachmentId % 100L);
    }
}