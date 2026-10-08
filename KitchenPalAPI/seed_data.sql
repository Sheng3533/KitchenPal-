-- KitchenPal 空模块种子数据：chefs / contents / videos
-- 执行：mysql -h localhost -uroot -psheng_885_test kitchenpal_db --default-character-set=utf8mb4 < seed_data.sql

-- ===== 大厨 chefs =====
INSERT INTO chefs (name, title, specialty, description, avatar, style, experience_years, signature_dish, status) VALUES
('王大厨', '川菜主厨', '川湘家常菜', '从业15年的川菜老师傅，擅长用家常调料做出地道川味，教学风格通俗易懂。', '', '川菜', 15, '麻婆豆腐', 1),
('李烘焙', '烘焙师', '西点烘焙', '专业烘焙师，专注免烤与家庭烤箱甜点，让新手也能轻松做出高颜值甜品。', '', '烘焙', 8, '提拉米苏', 1),
('张营养', '营养顾问', '健康轻食', '注册营养顾问，提倡低油低盐高蛋白的轻食搭配，帮助上班族吃得更健康。', '', '轻食', 10, '鸡胸蔬菜沙拉', 1);

-- ===== 内容 contents =====
INSERT INTO contents (title, content, type, cover_image, author, status, view_count, create_time, update_time) VALUES
('夏季清爽养生食谱推荐', '炎炎夏日容易食欲不振，本期为大家整理5道清爽开胃的养生食谱：凉拌黄瓜、冬瓜薏米汤、苦瓜炒蛋、绿豆百合羹、番茄豆腐汤。做法简单、热量低，适合全家享用。', 'article', '', 'KitchenPal编辑部', 1, 328, '2026-06-12 10:00:00', '2026-06-12 10:00:00'),
('新手下厨避坑指南', '第一次进厨房总手忙脚乱？记住这几点：①热锅冷油防粘锅；②炒菜先荤后素；③调味少量多次；④刀具生熟分开。收藏这篇，少走弯路。', 'article', '', 'KitchenPal编辑部', 1, 512, '2026-07-03 14:30:00', '2026-07-03 14:30:00'),
('KitchenPal 一周年活动公告', '感谢大家一年的陪伴！即日起到月底，分享你的拿手菜照片即有机会获得定制围裙与食材大礼包，详情见App内活动页。', 'announcement', '', 'KitchenPal运营', 1, 156, '2026-08-20 09:00:00', '2026-08-20 09:00:00');

-- ===== 视频 videos =====
INSERT INTO videos (title, description, video_url, cover_image, category, chef_id, chef_name, duration, difficulty, view_count, status, create_time) VALUES
('番茄炒蛋零失败教学', '从打蛋到火候，手把手教你做出鲜嫩不老的番茄炒蛋，新手必看。', 'https://www.bilibili.com/video/BV1xx411c7mD', '', '基础技巧', 1, '王大厨', '05:30', '简单', 1240, 1, '2026-06-15 11:00:00'),
('麻婆豆腐正宗做法', '川菜经典麻婆豆腐，麻辣鲜香嫩滑，掌握勾芡时机是关键。', 'https://www.bilibili.com/video/BV1xx411c7mE', '', '家常菜', 1, '王大厨', '08:12', '中等', 980, 1, '2026-06-18 16:20:00'),
('提拉米苏免烤版', '不用烤箱也能做的提拉米苏，马斯卡彭与咖啡酒香交融，入口即化。', 'https://www.bilibili.com/video/BV1xx411c7mF', '', '烘焙', 2, '李烘焙', '10:45', '中等', 760, 1, '2026-07-01 19:00:00'),
('10分钟快手鸡胸沙拉', '减脂期也能吃饱吃好，高蛋白低卡的鸡胸蔬菜沙拉，10分钟搞定。', 'https://www.bilibili.com/video/BV1xx411c7mG', '', '轻食', 3, '张营养', '06:08', '简单', 540, 1, '2026-07-10 12:30:00');
