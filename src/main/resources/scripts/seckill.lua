-- 秒杀原子预减库存脚本
-- KEYS[1] = seckill:stock:{goodsId}    库存 key
-- KEYS[2] = seckill:bought:{goodsId}   已购用户集合 key
-- ARGV[1] = userId                     用户ID
--
-- 返回码：
--   0  预减成功
--  -1  库存不足（售罄）
--  -2  重复秒杀（一人一单）

if (redis.call('sIsMember', KEYS[2], ARGV[1]) == 1) then
    return -2;
end

local stock = tonumber(redis.call('get', KEYS[1]));
if (stock == nil) or (stock <= 0) then
    return -1;
end

redis.call('decr', KEYS[1]);
redis.call('sAdd', KEYS[2], ARGV[1]);
return 0;
