/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : inspiration-space

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 11/11/2025 21:45:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `message_id` bigint NOT NULL COMMENT '消息ID（雪花ID）',
  `room_id` bigint NOT NULL COMMENT '所属会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者 user_id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '1:文本 2:图片 3:文件 4:系统消息',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1:正常 2:已撤回',
  `sent_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`message_id` DESC) USING BTREE,
  INDEX `idx_room_sent`(`room_id` ASC, `sent_at` ASC) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_sent_at`(`sent_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------

-- ----------------------------
-- Table structure for chat_room_members
-- ----------------------------
DROP TABLE IF EXISTS `chat_room_members`;
CREATE TABLE `chat_room_members`  (
  `room_id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '成员 user_id',
  `role` tinyint NOT NULL DEFAULT 1 COMMENT '1:普通成员 2:管理员 3:群主',
  `last_read_msg_id` bigint NULL DEFAULT 0 COMMENT '最后已读的消息ID（用于未读数计算）',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`room_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_joined_at`(`joined_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话成员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_room_members
-- ----------------------------

-- ----------------------------
-- Table structure for chat_rooms
-- ----------------------------
DROP TABLE IF EXISTS `chat_rooms`;
CREATE TABLE `chat_rooms`  (
  `room_id` bigint NOT NULL COMMENT '会话ID',
  `room_type` tinyint NOT NULL DEFAULT 1 COMMENT '1:群聊 2:单聊',
  `room_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '群聊名称；单聊可为空',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '群描述（单聊忽略）',
  `require_approval` tinyint(1) NULL DEFAULT 0 COMMENT '入群是否需审核（仅群聊有效）',
  `created_by` bigint NOT NULL COMMENT '创建者 user_id（群主）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`room_id`) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_type`(`room_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_rooms
-- ----------------------------

-- ----------------------------
-- Table structure for mod_feed_comments
-- ----------------------------
DROP TABLE IF EXISTS `mod_feed_comments`;
CREATE TABLE `mod_feed_comments`  (
  `id` bigint NOT NULL,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论人ID',
  `parent_comment_id` bigint NULL DEFAULT NULL COMMENT '父评论ID，NULL表示直接评论帖子',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评论内容',
  `like_count` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_time`(`post_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_comment_id` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_at` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mod_feed_comments
-- ----------------------------

-- ----------------------------
-- Table structure for mod_feed_post_stats
-- ----------------------------
DROP TABLE IF EXISTS `mod_feed_post_stats`;
CREATE TABLE `mod_feed_post_stats`  (
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `like_count` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞总数',
  `comment_count` int UNSIGNED NULL DEFAULT 0 COMMENT '评论总数',
  `repost_count` int UNSIGNED NULL DEFAULT 0 COMMENT '转发总数',
  `view_count` int UNSIGNED NULL DEFAULT 0 COMMENT '浏览量',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`) USING BTREE,
  INDEX `idx_like_count`(`like_count` DESC) USING BTREE,
  INDEX `idx_comment_count`(`comment_count` DESC) USING BTREE,
  INDEX `idx_repost_count`(`repost_count` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mod_feed_post_stats
-- ----------------------------

-- ----------------------------
-- Table structure for mod_feed_posts
-- ----------------------------
DROP TABLE IF EXISTS `mod_feed_posts`;
CREATE TABLE `mod_feed_posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '发帖人ID，逻辑关联 users.id',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '帖子内容，支持文字和emoji',
  `image_urls` json NULL COMMENT '图片URL数组，例如 [\"https://...jpg\", \"...\"]',
  `product_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品跳转链接（可选）',
  `visibility` tinyint NULL DEFAULT (0) COMMENT '可见范围(0公开，1私密，2好友可见)',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '软删除：0=正常，1=已删',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mod_feed_posts
-- ----------------------------

-- ----------------------------
-- Table structure for mod_feed_user_actions
-- ----------------------------
DROP TABLE IF EXISTS `mod_feed_user_actions`;
CREATE TABLE `mod_feed_user_actions`  (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型(1评论，2点赞，3收藏，4转发)',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `is_active` tinyint NULL DEFAULT 1 COMMENT '是否有效：1=激活，0=已取消',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_active`(`user_id` ASC, `is_active` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户行为记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mod_feed_user_actions
-- ----------------------------

-- ----------------------------
-- Table structure for notification_receivers
-- ----------------------------
DROP TABLE IF EXISTS `notification_receivers`;
CREATE TABLE `notification_receivers`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notification_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `read_status` tinyint NOT NULL DEFAULT 0,
  `read_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notification_id`(`notification_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification_receivers
-- ----------------------------

-- ----------------------------
-- Table structure for notification_templates
-- ----------------------------
DROP TABLE IF EXISTS `notification_templates`;
CREATE TABLE `notification_templates`  (
  `template_id` int NOT NULL AUTO_INCREMENT,
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `variables` json NOT NULL,
  `template_category` tinyint NOT NULL DEFAULT 1,
  `create_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`template_id`) USING BTREE,
  UNIQUE INDEX `template_name`(`template_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification_templates
-- ----------------------------

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NULL DEFAULT NULL,
  `message_type` tinyint NOT NULL,
  `template_id` int NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category` tinyint NOT NULL,
  `status` tinyint NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `scheduled_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_category_status`(`category` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for pay_orders
-- ----------------------------
DROP TABLE IF EXISTS `pay_orders`;
CREATE TABLE `pay_orders`  (
  `order_id` bigint NOT NULL COMMENT '内部订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商户系统唯一订单号',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方订单交易号',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单标题（如：VIP会员充值）',
  `content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单描述',
  `original_amount` decimal(10, 2) NOT NULL COMMENT '原金额（单位：元）',
  `discount_amount` decimal(10, 2) NULL COMMENT '优惠金额（单位：元）',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额（单位：元）',
  `product_type` tinyint NOT NULL COMMENT '产品类型：1=会员, 2=商品, ...',
  `trade_status` tinyint NOT NULL DEFAULT (0) COMMENT '交易状态（0创建，1已支付，2未支付，3退款）',
  `expire_time` datetime NOT NULL COMMENT '订单过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expired_at` datetime NULL DEFAULT NULL COMMENT '关闭时间（超时未付）',
  `PAY_at` datetime NULL DEFAULT NULL COMMENT '支付完成时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `trade_status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE,
  INDEX `idx_PAY_at`(`PAY_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_orders
-- ----------------------------

-- ----------------------------
-- Table structure for pay_refunds
-- ----------------------------
DROP TABLE IF EXISTS `pay_refunds`;
CREATE TABLE `pay_refunds`  (
  `refund_id` bigint NOT NULL,
  `order_id` bigint NOT NULL COMMENT '原订单ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `transaction_id` bigint NOT NULL COMMENT '原支付记录ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户退款单号',
  `trade_refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方退款单号',
  `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '退款原因',
  `status` tinyint NOT NULL DEFAULT (0) COMMENT '退款状态(0退款中，1成功，2失败，3取消)',
  `success_time` datetime NULL DEFAULT NULL COMMENT '退款成功时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`refund_id`) USING BTREE,
  UNIQUE INDEX `refund_no`(`refund_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_refund_no`(`refund_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_refunds
-- ----------------------------

-- ----------------------------
-- Table structure for pay_transactions
-- ----------------------------
DROP TABLE IF EXISTS `pay_transactions`;
CREATE TABLE `pay_transactions`  (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `transaction_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付平台交易号',
  `payment_method` tinyint NOT NULL COMMENT '支付渠道(1微信，2支付宝,3银行卡，4其他)',
  `amount` decimal(10, 2) NOT NULL COMMENT '本次支付金额',
  `status` tinyint NOT NULL DEFAULT (0) COMMENT '支付状态(0支付中，1成功，2失败，3取消)',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方交易号（如微信transaction_id）',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`) USING BTREE,
  UNIQUE INDEX `transaction_no`(`transaction_no` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_transaction_no`(`transaction_no` ASC) USING BTREE,
  INDEX `idx_trade_no`(`trade_no` ASC) USING BTREE,
  INDEX `idx_channel_status`(`payment_method` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付交易记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pay_transactions
-- ----------------------------

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `permission_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码（如 user:read）',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `permission_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`permission_id`) USING BTREE,
  UNIQUE INDEX `permission_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_code`(`permission_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, 'client:user:profile:view', '查看个人信息', '查看自己的个人资料', '2025-10-17 17:18:58', 1, '2025-10-18 16:48:21');
INSERT INTO `permission` VALUES (2, 'client:user:profile:edit', '修改个人信息', '编辑自己的个人资料', '2025-10-17 17:18:58', 1, '2025-10-18 16:48:26');
INSERT INTO `permission` VALUES (3, 'client:user:password:change', '修改密码', '修改自己的密码', '2025-10-17 17:18:58', 1, '2025-10-18 16:48:28');
INSERT INTO `permission` VALUES (4, 'client:user:delete', '注销', '注销账户', '2025-10-17 17:18:58', 1, '2025-10-18 17:23:18');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `role_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名（如 admin, user）',
  `role_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `role_name`(`role_name` ASC) USING BTREE,
  INDEX `idx_role_name`(`role_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'client-user', '普通用户', '普通用户，拥有用户端基础操作权限', '2025-10-17 16:51:53', '2025-10-19 13:28:21');
INSERT INTO `role` VALUES (2, 'admin-user', '用户模块管理员', '用户模块管理员，管理用户相关事务', '2025-10-17 16:52:37', '2025-10-19 13:28:27');
INSERT INTO `role` VALUES (3, 'admin-work', '作品模块管理员', '作品模块管理员，管理作品相关事务', '2025-10-17 16:52:37', '2025-10-19 13:28:35');
INSERT INTO `role` VALUES (4, 'admin-chat', '聊天模块管理员', '聊天模块管理员，管理聊天相关事务', '2025-10-17 16:52:37', '2025-10-19 13:28:45');
INSERT INTO `role` VALUES (5, 'admin-forum', '论坛模块管理员', '论坛模块管理员，管理论坛相关事务', '2025-10-17 16:52:37', '2025-10-19 13:28:52');
INSERT INTO `role` VALUES (6, 'admin-notification', '通知模块管理员', '通知模块管理员，管理通知相关事务', '2025-10-17 16:52:37', '2025-10-19 13:28:59');
INSERT INTO `role` VALUES (7, 'admin-payment', '支付模块管理员', '支付模块管理员，管理支付相关事务', '2025-10-17 16:52:37', '2025-10-19 13:29:06');
INSERT INTO `role` VALUES (8, 'super-admin', '超级管理员', '超级管理员，拥有系统所有管理权限', '2025-10-17 16:52:37', '2025-10-19 13:29:13');
INSERT INTO `role` VALUES (10, 'client-vip', 'VIP用户', 'VIP用户，拥有用户端基础操作权限', '2025-10-17 16:53:34', '2025-10-19 13:29:20');

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `role_id` bigint UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` bigint UNSIGNED NOT NULL COMMENT '权限ID',
  `grant_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE,
  INDEX `idx_role`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 1, '2025-10-17 17:22:11');
INSERT INTO `role_permission` VALUES (1, 2, '2025-10-17 17:22:11');
INSERT INTO `role_permission` VALUES (1, 3, '2025-10-17 17:22:11');
INSERT INTO `role_permission` VALUES (1, 4, '2025-10-17 17:22:11');

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签主表（无外键）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------

-- ----------------------------
-- Table structure for user_expansion
-- ----------------------------
DROP TABLE IF EXISTS `user_expansion`;
CREATE TABLE `user_expansion`  (
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `last_login_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后登录时间',
  `balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（单位：元）',
  `credit_score` int NOT NULL DEFAULT 100 COMMENT '信誉分，默认100',
  `user_status` tinyint NOT NULL DEFAULT 0 COMMENT '用户状态：0正常，1禁言，2封禁，3注销',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户动态信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_expansion
-- ----------------------------
INSERT INTO `user_expansion` VALUES (369804543023779840, '2025-10-19 12:56:48', 0.00, 100, 0, 0, '2025-10-17 19:11:14');
INSERT INTO `user_expansion` VALUES (370440795184041984, '2025-10-19 16:33:13', 0.00, 100, 0, 0, '2025-10-19 13:19:28');

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（11位）',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别：0未知，1男，2女',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户基本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_profile
-- ----------------------------
INSERT INTO `user_profile` VALUES (369804543023779840, 'lisi', '$2a$10$CwZWGNB6dUi3e8LRgE/GWOROvkso7lAnlWtFEmgR4KQwY4KrryMZe', '123456789@qq.com', '15412345678', 0, NULL, NULL, NULL, '2025-10-17 19:11:14');
INSERT INTO `user_profile` VALUES (370440795184041984, 'zhangsan', '$2a$10$FdJpQaQY.DbY7NxvTerK2O0/H8CMgFozRxa6ADdLGsYNAKXC6ehmW', '123456@qq.com', '15241231235', 1, '2025-10-19', 'hahaabab', '', '2025-10-19 13:19:28');

-- ----------------------------
-- Table structure for user_relationship
-- ----------------------------
DROP TABLE IF EXISTS `user_relationship`;
CREATE TABLE `user_relationship`  (
  `follower_id` bigint UNSIGNED NOT NULL COMMENT '关注者ID',
  `followee_id` bigint UNSIGNED NOT NULL COMMENT '被关注人ID',
  `follow_type` tinyint NOT NULL DEFAULT 1 COMMENT '关系类型（0无 1关注 2特别关注 3拉黑）',
  `follow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_id`, `followee_id`) USING BTREE,
  INDEX `idx_followee`(`followee_id` ASC) USING BTREE,
  INDEX `idx_follower`(`follower_id` ASC) USING BTREE,
  CONSTRAINT `chk_not_self_follow` CHECK (`follower_id` <> `followee_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_relationship
-- ----------------------------

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` bigint UNSIGNED NOT NULL COMMENT '角色ID',
  `grant_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_role`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (369804543023779840, 1, '2025-10-17 19:11:14');
INSERT INTO `user_role` VALUES (370440795184041984, 1, '2025-10-19 13:19:28');

-- ----------------------------
-- Table structure for user_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_stats`;
CREATE TABLE `user_stats`  (
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID，逻辑关联 user_profile',
  `likes_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `fans_count` int NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `followings_count` int NOT NULL DEFAULT 0 COMMENT '关注数',
  `favorites_count` int NOT NULL DEFAULT 0 COMMENT '收藏数',
  `posts_count` int NOT NULL DEFAULT 0 COMMENT '发布内容数',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_stats
-- ----------------------------
INSERT INTO `user_stats` VALUES (369804543023779840, 0, 0, 0, 0, 0, '2025-10-17 19:11:14');
INSERT INTO `user_stats` VALUES (370440795184041984, 0, 0, 0, 0, 0, '2025-10-19 13:19:28');

-- ----------------------------
-- Table structure for work_attachment
-- ----------------------------
DROP TABLE IF EXISTS `work_attachment`;
CREATE TABLE `work_attachment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件扩展名（如.pdf, .mp4）',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MIME类型（如application/pdf）',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件哈希值（SHA-256）',
  `bucket_name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'default' COMMENT 'MinIO bucket名称',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MinIO对象键',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `access_strategy` tinyint NOT NULL DEFAULT 0 COMMENT '访问策略：0=公开, 1=会员, 2=付费',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_id`(`work_id` ASC) USING BTREE,
  INDEX `idx_file_hash`(`file_hash` ASC) USING BTREE,
  INDEX `idx_access_strategy`(`access_strategy` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_attachment
-- ----------------------------

-- ----------------------------
-- Table structure for work_info
-- ----------------------------
DROP TABLE IF EXISTS `work_info`;
CREATE TABLE `work_info`  (
  `work_id` bigint NOT NULL AUTO_INCREMENT COMMENT '作品ID',
  `creator_id` bigint NOT NULL COMMENT '创作者ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作品标题',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作品类型（如: article, video, ebook, course）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '作品描述',
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `content` longtext CHARACTER SET tis620 COLLATE tis620_thai_ci NULL COMMENT '纯文本内容',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '价格（单位：元）',
  `access_strategy` tinyint NOT NULL DEFAULT 0 COMMENT '访问策略：0=免费,1=会员免费, 2=付费购买',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-草稿,1-审核中,2-已发布,3-已下架,4-审核拒绝',
  `visibility` tinyint NOT NULL DEFAULT 0 COMMENT '可见性：0-公开,1-仅自己可见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '软删除时间',
  `total_revenue` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '累计收益（元）',
  `total_sales` int NOT NULL DEFAULT 0 COMMENT '累计销量',
  PRIMARY KEY (`work_id`) USING BTREE,
  INDEX `idx_creator_status`(`creator_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status_publish`(`status` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_access_strategy`(`access_strategy` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作品信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_info
-- ----------------------------

-- ----------------------------
-- Table structure for work_stats
-- ----------------------------
DROP TABLE IF EXISTS `work_stats`;
CREATE TABLE `work_stats`  (
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `view_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `favorite_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `purchase_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '购买数',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`work_id`) USING BTREE,
  INDEX `idx_work_id`(`work_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作品统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_stats
-- ----------------------------

-- ----------------------------
-- Table structure for work_tag
-- ----------------------------
DROP TABLE IF EXISTS `work_tag`;
CREATE TABLE `work_tag`  (
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`work_id`, `tag_id`) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '作品标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_tag
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
