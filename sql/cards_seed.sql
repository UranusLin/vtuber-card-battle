-- ═══════════════════════════════════════════════════════════
-- VTuber Card Battle - 資料庫初始化腳本
-- 執行方式: psql -U postgres -d vcb -f cards_seed.sql
-- ═══════════════════════════════════════════════════════════

-- 清理舊資料（如果重新建立）
DROP TABLE IF EXISTS match_history;
DROP TABLE IF EXISTS deck_cards;
DROP TABLE IF EXISTS decks;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS cards;

-- ═══ 建立資料表 ═══

CREATE TABLE cards (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    faction VARCHAR(30) NOT NULL,
    card_type VARCHAR(15) NOT NULL CHECK (card_type IN ('VTUBER','TALENT','SUPPORT')),
    mana_cost INT NOT NULL,
    attack INT DEFAULT 0,
    health INT DEFAULT 0,
    rarity INT NOT NULL CHECK (rarity BETWEEN 1 AND 4),
    ability VARCHAR(20),
    effect_type VARCHAR(30),
    effect_value INT DEFAULT 0,
    target_type VARCHAR(20),
    effect_desc TEXT,
    flavor_text TEXT
);

CREATE TABLE players (
    id SERIAL PRIMARY KEY,
    username VARCHAR(30) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    favorite_faction VARCHAR(30),
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE decks (
    id SERIAL PRIMARY KEY,
    player_id INT NOT NULL REFERENCES players(id),
    deck_name VARCHAR(50) NOT NULL,
    faction VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE deck_cards (
    deck_id INT REFERENCES decks(id) ON DELETE CASCADE,
    card_id INT REFERENCES cards(id),
    quantity INT DEFAULT 1 CHECK (quantity BETWEEN 1 AND 2),
    PRIMARY KEY (deck_id, card_id)
);

CREATE TABLE match_history (
    id SERIAL PRIMARY KEY,
    player_id INT NOT NULL REFERENCES players(id),
    opponent_name VARCHAR(50) NOT NULL,
    player_faction VARCHAR(30),
    opponent_faction VARCHAR(30),
    result VARCHAR(5) NOT NULL CHECK (result IN ('WIN','LOSE')),
    turns INT,
    player_hp_left INT,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ═══════════════════════════════════════════════════════════
-- 卡牌資料匯入
-- ═══════════════════════════════════════════════════════════

-- ════════════ ✨ 極深空計畫 (DEEP_SPACE) ════════════

-- VTuber 卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('厄倫蒂兒', 'DEEP_SPACE', 'VTUBER', 5, 4, 6, 4, NULL, 'HEAL_ALL_ALLY', 1, NULL, '回合結束恢復全體友方 1 HP', '來自最遙遠恆星的療癒歌聲'),
('涅默', 'DEEP_SPACE', 'VTUBER', 4, 5, 4, 3, NULL, 'BUFF_CONDITIONAL', 2, NULL, '場上有其他極深空成員時 ATK +2', '因為有你才能存在的太陽伴星'),
('埃穆亞', 'DEEP_SPACE', 'VTUBER', 3, 3, 4, 3, 'BATTLE_CRY', 'DRAW', 1, NULL, '戰吼：抽 1 張牌', '來自遠方的信使帶來了新的訊息'),
('熙歌', 'DEEP_SPACE', 'VTUBER', 6, 6, 5, 4, 'BATTLE_CRY', 'BOUNCE', 1, 'SINGLE_ENEMY', '戰吼：將 1 隻敵方送回手牌', '黑洞的傳送門將一切吸入虛空');

-- 才藝卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('歌回直播', 'DEEP_SPACE', 'TALENT', 3, 0, 0, 2, NULL, 'HEAL', 5, 'HERO', '恢復己方英雄 5 HP', '今晚的歌聲特別溫暖'),
('天文台連動', 'DEEP_SPACE', 'TALENT', 4, 0, 0, 3, NULL, 'DAMAGE', 2, 'ALL_ENEMIES', '對全體敵方造成 2 點傷害', '星空的力量降臨戰場'),
('環球音樂出道', 'DEEP_SPACE', 'TALENT', 7, 0, 0, 4, NULL, 'SUMMON_RANDOM', 1, 'FACTION', '從牌組召喚 1 名隨機極深空成員', '正式出道的那一刻全世界都在看');

-- 應援卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('湯圓碗應援棒', 'DEEP_SPACE', 'SUPPORT', 2, 2, 0, 2, NULL, NULL, NULL, NULL, '+2 ATK', '湯圓碗們的應援聲震天'),
('極深空專輯', 'DEEP_SPACE', 'SUPPORT', 3, 1, 3, 3, NULL, NULL, NULL, NULL, '+1 ATK / +3 HP', '首張同名原創專輯');

-- ════════════ 🐾 瀕臨絕種團 (RESCUTE) ════════════

-- VTuber 卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('15號', 'RESCUTE', 'VTUBER', 3, 4, 3, 3, NULL, 'GAIN_MANA_ON_KILL', 1, NULL, '擊殺敵方時獲得 +1 魔力水晶', '成為偶像賺大錢買下家園保護同胞！'),
('露恰露恰', 'RESCUTE', 'VTUBER', 4, 2, 6, 3, 'TAUNT', 'HEAL_ALL_ALLY', 1, NULL, '嘲諷。回合結束恢復友方全體 1 HP', '大姊永遠擋在最前面照顧大家'),
('歐貝爾', 'RESCUTE', 'VTUBER', 5, 5, 7, 4, 'TAUNT', 'COUNTER_ATTACK', 50, NULL, '嘲諷。被攻擊時 50% 機率反擊', '雖然超級緊張但還是勇敢地站出來了！');

-- 才藝卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('保育宣導', 'RESCUTE', 'TALENT', 2, 0, 0, 2, NULL, 'BUFF_HP', 3, 'SINGLE_ALLY', '一隻友方隨從 +0 ATK / +3 HP', '讓更多人認識瀕危動物的重要性'),
('闆闆出動', 'RESCUTE', 'TALENT', 4, 0, 0, 3, NULL, 'HEAL_FULL', 0, 'SINGLE_ALLY', '恢復一隻友方隨從至滿血', '闆闆永遠是最強後盾'),
('瀕危動物之力', 'RESCUTE', 'TALENT', 5, 0, 0, 3, NULL, 'GRANT_TAUNT', 0, 'ALL_ALLIES', '全體友方隨從獲得嘲諷', '瀕危但絕不退縮');

-- 應援卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('石虎路殺警示牌', 'RESCUTE', 'SUPPORT', 1, 0, 2, 1, NULL, NULL, NULL, NULL, '+0 ATK / +2 HP', '注意石虎出沒'),
('保育大使徽章', 'RESCUTE', 'SUPPORT', 3, 1, 2, 3, 'TAUNT', NULL, NULL, NULL, '+1 ATK / +2 HP 並獲得嘲諷', '戴上徽章的那刻起就是守護者');

-- ════════════ 🌤️ 瑟拉斯蒂歐 (CELESTIAL) ════════════

-- VTuber 卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('雲隙光', 'CELESTIAL', 'VTUBER', 3, 3, 3, 2, 'CHARGE', NULL, NULL, NULL, '衝鋒', '穿透雲層的瞬間萬丈光芒'),
('冰霧', 'CELESTIAL', 'VTUBER', 4, 2, 5, 3, 'BATTLE_CRY', 'FREEZE', 1, 'SINGLE_ENEMY', '戰吼：凍結 1 隻敵方（跳過下回合）', '極寒之霧覆蓋一切'),
('白白虹', 'CELESTIAL', 'VTUBER', 3, 2, 4, 2, NULL, 'THORNS', 1, NULL, '受傷時對攻擊者造成 1 點反傷', '七彩折射的光芒會灼傷觸碰者'),
('幻月', 'CELESTIAL', 'VTUBER', 5, 4, 5, 3, NULL, 'PHASE_SHIFT', 2, NULL, '每回合在 +2 ATK / +2 HP 間切換', '月相更迭帶來不同的力量');

-- 才藝卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('暴風預警', 'CELESTIAL', 'TALENT', 3, 0, 0, 2, NULL, 'DAMAGE', 4, 'SINGLE_ENEMY', '對單一目標造成 4 點傷害', '天氣預報：大暴風即將來襲'),
('天氣異常', 'CELESTIAL', 'TALENT', 5, 0, 0, 4, NULL, 'FREEZE', 0, 'ALL_ENEMIES', '凍結全體敵方（跳過下回合）', '異常寒流席捲整個戰場'),
('極光降臨', 'CELESTIAL', 'TALENT', 4, 0, 0, 3, NULL, 'HEAL_AND_DRAW', 3, 'HERO', '恢復己方英雄 3 HP 並抽 1 張牌', '極光的美麗蘊含著能量');

-- 應援卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('氣象觀測儀', 'CELESTIAL', 'SUPPORT', 2, 1, 1, 2, NULL, NULL, NULL, NULL, '+1 ATK / +1 HP', '精準掌握天氣脈動'),
('天象護符', 'CELESTIAL', 'SUPPORT', 3, 0, 4, 3, NULL, NULL, NULL, NULL, '+0 ATK / +4 HP', '天象之力凝結的護符');

-- ════════════ ⚔️ 諦覓司 (TIMAEUS) ════════════

-- VTuber 卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('賽特珞', 'TIMAEUS', 'VTUBER', 4, 5, 3, 3, 'CHARGE', NULL, NULL, NULL, '衝鋒', '最快的劍就是最強的劍'),
('艾斯珀達', 'TIMAEUS', 'VTUBER', 3, 4, 3, 2, NULL, 'EXTRA_ATTACK_ON_KILL', 0, NULL, '擊殺目標後可再攻擊一次', '一劍既出必取敵首'),
('格萊伊', 'TIMAEUS', 'VTUBER', 5, 3, 6, 3, 'TAUNT', 'DAMAGE_ALL_ENEMY_ON_DEATH', 2, NULL, '嘲諷。亡語：對全體敵方 2 傷害', '聖杯碎裂時光芒將吞噬一切'),
('穆恩佐', 'TIMAEUS', 'VTUBER', 6, 7, 5, 4, 'BATTLE_CRY', 'EXECUTE', 3, 'SINGLE_ENEMY', '戰吼：消滅 1 隻 HP ≤ 3 的敵方', '終焉之刃不留活口');

-- 才藝卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('武器強化', 'TIMAEUS', 'TALENT', 2, 0, 0, 2, NULL, 'BUFF_ATK', 3, 'SINGLE_ALLY', '一隻友方隨從 +3 ATK', '鍛造師的極致工藝'),
('斷鋼一擊', 'TIMAEUS', 'TALENT', 4, 0, 0, 3, NULL, 'DAMAGE', 6, 'SINGLE_ENEMY', '對單一目標造成 6 點傷害', '能斬斷鋼鐵的究極一刀'),
('四劍合璧', 'TIMAEUS', 'TALENT', 6, 0, 0, 4, NULL, 'BUFF_ATK', 2, 'ALL_ALLIES', '全體友方隨從 +2 ATK', '四把聖器同時共鳴的力量');

-- 應援卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('鍛造之錘', 'TIMAEUS', 'SUPPORT', 2, 3, 0, 2, NULL, NULL, NULL, NULL, '+3 ATK / +0 HP', '千錘百鍊的極致'),
('聖器碎片', 'TIMAEUS', 'SUPPORT', 3, 2, 2, 3, NULL, NULL, NULL, NULL, '+2 ATK / +2 HP', '古老聖器殘存的力量');

-- ════════════ 🔮 五律悖反 (PARADOX) ════════════

-- VTuber 卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('拉斐利婭', 'PARADOX', 'VTUBER', 5, 4, 5, 4, NULL, 'COMBO_BUFF', 3, NULL, '場上有安特羅迦時雙方 ATK +3', '悖論的一面——光芒'),
('安特羅迦', 'PARADOX', 'VTUBER', 5, 5, 4, 4, 'DEATH_RATTLE', 'REVIVE_SWAPPED', 0, NULL, '亡語：以 ATK/HP 互換的狀態復活', '悖論的另一面——因果反轉');

-- 才藝卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('時空悖論', 'PARADOX', 'TALENT', 4, 0, 0, 3, NULL, 'SWAP_STATS', 0, 'SINGLE_ENEMY', '將 1 隻敵方的 ATK 和 HP 互換', '因果律在此刻崩壞'),
('維度裂縫', 'PARADOX', 'TALENT', 6, 0, 0, 4, NULL, 'SUMMON_RANDOM', 1, 'ANY', '從任意陣營隨機召喚 1 隻隨從', '裂縫彼端不知道會出現什麼'),
('物理常數崩壞', 'PARADOX', 'TALENT', 3, 0, 0, 2, NULL, 'DAMAGE_RANDOM', 6, 'SINGLE_ENEMY', '隨機對 1 隻敵方造成 1~6 點傷害', '物理定律已不再適用');

-- 應援卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('悖論結晶', 'PARADOX', 'SUPPORT', 2, 2, 1, 2, NULL, NULL, NULL, NULL, '+2 ATK / +1 HP', '悖論凝結而成的晶體');

-- ════════════ 🎴 中立 (NEUTRAL) ════════════

-- 才藝卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('SC 紅色超級留言', 'NEUTRAL', 'TALENT', 2, 0, 0, 1, NULL, 'HEAL', 3, 'HERO', '恢復己方英雄 3 HP', '觀眾的愛心超級留言'),
('聯動企劃', 'NEUTRAL', 'TALENT', 3, 0, 0, 2, NULL, 'DRAW', 2, NULL, '抽 2 張牌', '跨團聯動帶來新的可能性'),
('YT 黃標危機', 'NEUTRAL', 'TALENT', 4, 0, 0, 3, NULL, 'DESTROY_RANDOM', 1, 'ANY', '隨機消滅場上 1 隻隨從（敵我皆可能）', '黃標降臨誰都無法倖免'),
('百萬訂閱達成', 'NEUTRAL', 'TALENT', 8, 0, 0, 4, NULL, 'DAMAGE', 10, 'ENEMY_HERO', '對敵方英雄造成 10 點傷害', '一百萬人的力量匯聚而成的一擊'),
('剪輯精華上片', 'NEUTRAL', 'TALENT', 1, 0, 0, 1, NULL, 'DRAW', 1, NULL, '抽 1 張牌', '精華剪輯讓更多人認識你');

-- 應援卡
INSERT INTO cards (name, faction, card_type, mana_cost, attack, health, rarity, ability, effect_type, effect_value, target_type, effect_desc, flavor_text) VALUES
('螢光棒', 'NEUTRAL', 'SUPPORT', 1, 1, 0, 1, NULL, NULL, NULL, NULL, '+1 ATK', '應援的基本配備'),
('會限周邊 T-shirt', 'NEUTRAL', 'SUPPORT', 2, 0, 3, 2, NULL, NULL, NULL, NULL, '+0 ATK / +3 HP', '限定周邊的防護力'),
('3D 演唱會門票', 'NEUTRAL', 'SUPPORT', 4, 3, 2, 3, NULL, NULL, NULL, NULL, '+3 ATK / +2 HP', '3D LIVE 的震撼體驗');

-- ═══════════════════════════════════════════════════════════
-- 驗證匯入結果
-- ═══════════════════════════════════════════════════════════

SELECT
    faction AS 陣營,
    card_type AS 類型,
    COUNT(*) AS 張數
FROM cards
GROUP BY faction, card_type
ORDER BY faction, card_type;

SELECT '✅ 卡牌總數: ' || COUNT(*) || ' 張' AS result FROM cards;
