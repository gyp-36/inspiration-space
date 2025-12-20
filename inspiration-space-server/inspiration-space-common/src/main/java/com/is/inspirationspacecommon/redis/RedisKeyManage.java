package com.is.inspirationspacecommon.redis;


import lombok.Getter;

/**
 * @description: Redis key模版
 * @author:
 **/
@Getter
public enum RedisKeyManage {
    WORK_COUNT("work_count:","作品统计id","value为作品统计"),
    USER_LOGIN("user_login_%s_%s","用户登录","value为UserVo类型"),
    USER_TOKENS("user_tokens:%s","用户token","value为UserLoginVo类型"),
    FORUM_POST_STATS("forum_post_stats:%s","帖子统计id","value为帖子统计"),

    //分布式datacenter_id
    DISTRIBUTED_DATACENTER_ID("distributed_datacenter_id:%s","分布式datacenter_id","分布式datacenter_id的值"),

    ALL_RULE_HASH("all_rule_hash","所有规则的key","所有规则的Hash"),
    RULE("rule","调用限制规则的key","调用限制规则的value"),

    RULE_LIMIT("rule_limit_%s","调用限制时间的key","调用限制时间的value"),

    Z_SET_RULE_STAT("z_set_rule_stat_%s","规则zset", "value为zset类型"),

    DEPTH_RULE("depth_rule","深度调用限制规则的key","深度调用限制规则的value"),

    DEPTH_RULE_LIMIT("depth_rule_limit_%s_%s","深度调用限制时间的key","深度调用限制时间的value"),


    PLATFORM_NOTICE_FLAG("platform_notice_flag","platform_notice_flag的key","platform_notice_flag的value"),

    WORK_SALES_RANK_DAY("work_sales_rank_day","日销量排行榜" ,"销量前10" ),
    ORDER_DRAFT_REQUEST("order_request_%s_%s","订单key","订单号"),
    ORDER_DRAFT("order_draft_%s","订单Id" ,"订单Id" ),
    USER_ORDER("user_order_%s","用户临时订单" ,"用户临时订单" ),
    USER_ONLINE("user_online_%s", "用户在线" , "用户ID" ),

    // 在 RedisKeyManage 枚举中添加以下
    OFFLINE_MESSAGE_STREAM("offline_message_stream:%s", "离线消息Stream", "按receiverId分组的离线消息Stream"),
    OFFLINE_MESSAGE_CONSUMER_GROUP("offline_message_group", "离线消息消费组", "处理离线消息的消费组");

    /**
     * key值
     * */
    private final String key;

    /**
     * key的说明
     * */
    private final String keyIntroduce;

    /**
     * value的说明
     * */
    private final String valueIntroduce;


    RedisKeyManage(String key, String keyIntroduce, String valueIntroduce){
        this.key = key;
        this.keyIntroduce = keyIntroduce;
        this.valueIntroduce = valueIntroduce;
    }



}

