-- =============================================================================
-- 高并发秒杀系统 数据库初始化脚本
-- 兼容 MySQL 5.7 / 8.x，字符集 utf8mb4
-- 执行方式：mysql -uroot -p < seckill.sql
-- =============================================================================

-- 删除并重建数据库（首次执行使用；已有数据请勿在生产环境执行）
drop database if exists seckill;
create database seckill default character set utf8mb4 collate utf8mb4_general_ci;
use seckill;

-- -----------------------------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------------------------
drop table if exists seckill_user;
create table seckill_user (
    id            bigint unsigned not null auto_increment comment '用户ID',
    nickname      varchar(40)  not null default '' comment '昵称',
    mobile        varchar(20)  not null comment '手机号（登录账号）',
    password      char(32)     not null comment '登录密码 MD5(salt+明文)',
    salt          varchar(16)  not null default '' comment '密码盐',
    head          varchar(255) not null default '' comment '头像地址',
    register_date datetime     not null default current_timestamp comment '注册时间',
    primary key (id),
    unique key uk_mobile (mobile)
) engine = innodb default charset = utf8mb4 comment '秒杀用户表';

-- -----------------------------------------------------------------------------
-- 2. 秒杀商品表
--    stock_count 库存；秒杀依赖 start_date / end_date 判断状态
-- -----------------------------------------------------------------------------
drop table if exists seckill_goods;
create table seckill_goods (
    id            bigint unsigned not null auto_increment comment '商品ID',
    goods_name    varchar(100)   not null default '' comment '商品名称',
    goods_title   varchar(200)   not null default '' comment '商品标题',
    goods_img     varchar(255)   not null default '' comment '商品图片',
    goods_detail  varchar(500)   not null default '' comment '商品详情',
    goods_price   decimal(10, 2) not null default 0.00 comment '商品原价',
    seckill_price decimal(10, 2) not null default 0.00 comment '秒杀价',
    stock_count   int            not null default 0 comment '库存数量',
    start_date    datetime       not null comment '秒杀开始时间',
    end_date      datetime       not null comment '秒杀结束时间',
    primary key (id),
    key idx_start_date (start_date),
    key idx_end_date (end_date)
) engine = innodb default charset = utf8mb4 comment '秒杀商品表';

-- -----------------------------------------------------------------------------
-- 3. 秒杀订单表
--    唯一索引 uk_user_goods(user_id, goods_id)：数据库层面保证一人一单
-- -----------------------------------------------------------------------------
drop table if exists seckill_order;
create table seckill_order (
    id          bigint unsigned not null comment '订单ID（应用生成，雪花ID）',
    user_id     bigint unsigned not null comment '用户ID',
    goods_id    bigint unsigned not null comment '商品ID',
    pay_amount  decimal(10, 2)  not null default 0.00 comment '支付金额',
    status      tinyint         not null default 0 comment '状态：0未支付 1已支付 2已取消',
    create_date datetime        not null default current_timestamp comment '创建时间',
    pay_date    datetime        null comment '支付时间',
    primary key (id),
    unique key uk_user_goods (user_id, goods_id),
    key idx_goods_id (goods_id),
    key idx_status (status)
) engine = innodb default charset = utf8mb4 comment '秒杀订单表';

-- =============================================================================
-- 初始化数据
-- 注意：时间以脚本执行时刻为基准，保证商品1为“进行中”、商品2为“已结束”、商品3/4为“未开始”
-- =============================================================================

-- 测试用户：明文密码均为 123456，password = MD5(salt + '123456')
insert into seckill_user (id, nickname, mobile, password, salt, head, register_date) values
(1001, '张三', '13800000001', 'c9cc715c0c48834383a7f56a7233eac9', 'a1b2c3d4', '', now()),
(1002, '李四', '13800000002', 'dbc13cbd9432c968c29ab60445916a93', 'e5f6g7h8', '', now()),
(1003, '王五', '13800000003', '8d788e77fec454c5cd45843c75724451', 'i9j0k1l2', '', now());

-- 商品1：进行中，库存 10（适合压测）
insert into seckill_goods
(goods_name, goods_title, goods_img, goods_detail, goods_price, seckill_price, stock_count, start_date, end_date)
values
('iPhone 16 Pro', '苹果旗舰 5G 手机 256G', '', 'A18 芯片 / 钛金属机身 / 三摄系统',
 8999.00, 6999.00, 10, date_sub(now(), interval 1 minute), date_add(now(), interval 2 hour));

-- 商品2：已结束
insert into seckill_goods
(goods_name, goods_title, goods_img, goods_detail, goods_price, seckill_price, stock_count, start_date, end_date)
values
('小米15', '骁龙8至尊版 徕卡光学', '', '骁龙8 Elite / 徕卡三摄 / 澎湃OS',
 4499.00, 3299.00, 5, date_sub(now(), interval 2 hour), date_sub(now(), interval 1 hour));

-- 商品3：5 分钟后开始
insert into seckill_goods
(goods_name, goods_title, goods_img, goods_detail, goods_price, seckill_price, stock_count, start_date, end_date)
values
('华为 Mate 70 Pro', '鸿蒙 NEXT 红枫原色影像', '', '麒麟芯片 / 玄武架构 / 卫星通信',
 6999.00, 5499.00, 20, date_add(now(), interval 5 minute), date_add(now(), interval 3 hour));

-- 商品4：10 分钟后开始，大库存（适合 JMeter 高并发压测）
insert into seckill_goods
(goods_name, goods_title, goods_img, goods_detail, goods_price, seckill_price, stock_count, start_date, end_date)
values
('AirPods Pro 3', '主动降噪无线耳机', '', 'H3 芯片 / 自适应降噪 / 空间音频',
 1899.00, 1299.00, 100, date_add(now(), interval 10 minute), date_add(now(), interval 4 hour));

-- =============================================================================
-- 超卖防护说明：
-- 1. 应用层：Redis Lua 原子预减库存（DECR）+ 一人一单（SISMEMBER）
-- 2. 数据库层：update seckill_goods set stock_count = stock_count - 1
--             where id = ? and stock_count > 0   （条件更新，最终防超卖）
-- 3. 一人一单：seckill_order 上 (user_id, goods_id) 唯一索引兜底
-- =============================================================================
