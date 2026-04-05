-- =============================================================
--  DUNGEON REALM — schema.sql
--  Compatible with Railway MySQL (database already created).
--  No CREATE DATABASE / USE statements — Railway manages that.
-- =============================================================

-- ─────────────────────────────────────────────
--  1. USERS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login    DATETIME,
    is_online     TINYINT(1) DEFAULT 0
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  2. ROOMS / MAP
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rooms (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    north_id    INT,
    south_id    INT,
    east_id     INT,
    west_id     INT,
    room_type   ENUM('town','dungeon','boss','safe') DEFAULT 'dungeon',
    danger_level INT DEFAULT 1,
    FOREIGN KEY (north_id) REFERENCES rooms(id) ON DELETE SET NULL,
    FOREIGN KEY (south_id) REFERENCES rooms(id) ON DELETE SET NULL,
    FOREIGN KEY (east_id)  REFERENCES rooms(id) ON DELETE SET NULL,
    FOREIGN KEY (west_id)  REFERENCES rooms(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  3. PLAYER STATS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS player_stats (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL UNIQUE,
    current_room INT NOT NULL DEFAULT 1,
    hp           INT NOT NULL DEFAULT 100,
    max_hp       INT NOT NULL DEFAULT 100,
    mp           INT NOT NULL DEFAULT 50,
    max_mp       INT NOT NULL DEFAULT 50,
    attack       INT NOT NULL DEFAULT 10,
    defense      INT NOT NULL DEFAULT 5,
    level        INT NOT NULL DEFAULT 1,
    xp           INT NOT NULL DEFAULT 0,
    xp_next      INT NOT NULL DEFAULT 100,
    gold         INT NOT NULL DEFAULT 50,
    kills        INT NOT NULL DEFAULT 0,
    deaths       INT NOT NULL DEFAULT 0,
    in_combat    TINYINT(1) DEFAULT 0,
    current_monster_id INT,
    FOREIGN KEY (user_id)      REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (current_room) REFERENCES rooms(id)  ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  4. ITEMS (master catalogue)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description TEXT          NOT NULL,
    item_type   ENUM('weapon','armor','consumable','quest','treasure') NOT NULL,
    attack_bonus  INT DEFAULT 0,
    defense_bonus INT DEFAULT 0,
    hp_restore    INT DEFAULT 0,
    mp_restore    INT DEFAULT 0,
    gold_value    INT DEFAULT 10,
    rarity        ENUM('common','uncommon','rare','epic','legendary') DEFAULT 'common',
    icon          VARCHAR(50)  DEFAULT '⚔️'
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  5. INVENTORY
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id   INT NOT NULL,
    item_id   INT NOT NULL,
    quantity  INT NOT NULL DEFAULT 1,
    equipped  TINYINT(1) DEFAULT 0,
    obtained_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id)  ON DELETE CASCADE,
    UNIQUE KEY uq_user_item (user_id, item_id)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  6. MONSTERS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS monsters (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    hp          INT NOT NULL DEFAULT 30,
    attack      INT NOT NULL DEFAULT 8,
    defense     INT NOT NULL DEFAULT 2,
    xp_reward   INT NOT NULL DEFAULT 20,
    gold_reward INT NOT NULL DEFAULT 10,
    min_level   INT NOT NULL DEFAULT 1,
    loot_item_id INT,
    loot_chance DECIMAL(5,2) DEFAULT 0.30,
    icon        VARCHAR(10) DEFAULT '👹',
    FOREIGN KEY (loot_item_id) REFERENCES items(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  7. GAME LOG
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS game_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    event_type ENUM('move','combat','loot','chat','system','level_up','death') NOT NULL,
    message    TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  8. CHAT
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    username   VARCHAR(50) NOT NULL,
    message    TEXT NOT NULL,
    channel    VARCHAR(20) DEFAULT 'global',
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_channel_created (channel, created_at)
) ENGINE=InnoDB;

-- ─────────────────────────────────────────────
--  9. LEADERBOARD VIEW
-- ─────────────────────────────────────────────
CREATE OR REPLACE VIEW leaderboard AS
SELECT
    u.id,
    u.username,
    ps.level,
    ps.xp,
    ps.gold,
    ps.kills,
    ps.deaths,
    u.is_online,
    RANK() OVER (ORDER BY ps.level DESC, ps.xp DESC) AS rank_pos
FROM users u
JOIN player_stats ps ON ps.user_id = u.id
ORDER BY rank_pos;

-- ═══════════════════════════════════════════════
--  SAMPLE DATA  (INSERT IGNORE = safe to re-run)
-- ═══════════════════════════════════════════════

INSERT IGNORE INTO rooms (id, name, description, north_id, south_id, east_id, west_id, room_type, danger_level) VALUES
(1,  'Village of Ashenveil',
     'A crumbling village at the edge of the cursed forest. Torches flicker against the eternal night. A weathered signpost points toward the dungeon entrance.',
     2, NULL, 3, NULL, 'safe', 0),
(2,  'Dark Forest Path',
     'Gnarled trees claw at the moonless sky. Strange noises echo from the shadows. The path splits in several directions.',
     4, 1, NULL, NULL, 'dungeon', 1),
(3,  'Eastern Market Ruins',
     'Ruined market stalls lie toppled, treasures scattered among the debris. A locked chest sits in the corner.',
     NULL, NULL, NULL, 1, 'dungeon', 1),
(4,  'Dungeon Entrance',
     'Massive iron doors hang ajar, carved with warnings in ancient script. Cold air drifts from within. This is the point of no return.',
     5, 2, 6, 7, 'dungeon', 2),
(5,  'Hall of Bones',
     'Skeletal remains litter the floor. The walls are stained with the evidence of countless battles. A faint light pulses from the far end.',
     8, 4, NULL, NULL, 'dungeon', 3),
(6,  'Prison Cells',
     'Rusty iron bars line the walls. Chains dangle from the ceiling. Something skitters in the darkness beyond.',
     NULL, NULL, NULL, 4, 'dungeon', 2),
(7,  'Guard Tower Base',
     'The base of a collapsed tower. Arrow slits pierce the walls, offering narrow glimpses into adjacent chambers.',
     NULL, NULL, 4, NULL, 'dungeon', 2),
(8,  'Crypt of the Fallen',
     'Ancient sarcophagi line the walls, their lids ajar. The air smells of old magic and decay. A spiral stair descends into darkness.',
     9, 5, NULL, NULL, 'dungeon', 4),
(9,  'Underground Lake',
     'An impossibly vast cavern opens around a black, still lake. Your torch reflects in its surface, revealing shapes moving beneath.',
     NULL, 8, 10, NULL, 'dungeon', 4),
(10, 'Throne of Shadows — Boss Chamber',
     'A massive obsidian throne dominates the chamber. Hellfire burns in iron braziers. The Lich King turns slowly to face you...',
     NULL, 9, NULL, NULL, 'boss', 5);

INSERT IGNORE INTO items (id, name, description, item_type, attack_bonus, defense_bonus, hp_restore, mp_restore, gold_value, rarity, icon) VALUES
(1,  'Rusty Dagger',       'A chipped blade, better than bare hands.',      'weapon', 3, 0, 0, 0, 5,   'common',    '🗡️'),
(2,  'Short Sword',        'A reliable iron blade favoured by adventurers.', 'weapon', 7, 0, 0, 0, 30,  'common',    '⚔️'),
(3,  'Elven Longsword',    'Forged by elves, supernaturally sharp.',         'weapon', 14,0, 0, 0, 120, 'rare',      '⚔️'),
(4,  'Shadow Blade',       'Drinks the light around it. Terrifying.',        'weapon', 22,0, 0, 0, 350, 'epic',      '🌑'),
(5,  'Leviathan Edge',     'The sword of a dead god. Radiates dread power.', 'weapon', 35,0, 0, 0, 800, 'legendary', '⚡'),
(6,  'Leather Armor',      'Basic protection, better than cloth.',           'armor',  0, 3, 0, 0, 15,  'common',    '🧥'),
(7,  'Chainmail Hauberk',  'Interlocked rings provide solid protection.',    'armor',  0, 7, 0, 0, 80,  'uncommon',  '🛡️'),
(8,  'Plate Armor',        'Heavy steel plates. Slows you, but youll live.', 'armor',  0,14, 0, 0, 250, 'rare',      '🛡️'),
(9,  'Dragonscale Mail',   'Scales from a slain dragon. Near impenetrable.', 'armor',  0,22, 0, 0, 600, 'epic',      '🐉'),
(10, 'Health Potion',      'Restores 40 HP. Tastes of copper and herbs.',    'consumable',0,0,40, 0, 25, 'common',   '🧪'),
(11, 'Greater Health Pot', 'Restores 80 HP. Glows a deep crimson.',          'consumable',0,0,80, 0, 60, 'uncommon', '🧪'),
(12, 'Elixir of Life',     'Fully restores HP. Extremely rare.',             'consumable',0,0,999,0,200,'rare',      '✨'),
(13, 'Mana Potion',        'Restores 30 MP. Smells of ozone.',               'consumable',0,0, 0,30, 20, 'common',  '💧'),
(14, 'Gold Coin Pouch',    'A fat pouch of gold coins. 25 gold inside.',     'treasure',0,0,  0, 0, 25,  'common',  '💰'),
(15, 'Ancient Relic',      'A mysterious artefact of unknown power.',        'quest',  0, 0,  0, 0, 500, 'epic',    '🔮');

INSERT IGNORE INTO monsters (id, name, description, hp, attack, defense, xp_reward, gold_reward, min_level, loot_item_id, loot_chance, icon) VALUES
(1,  'Giant Rat',       'A bloated, mangy rat the size of a dog.',           20,  5, 1,  10, 3,  1, 10, 0.25, '🐀'),
(2,  'Skeleton Archer', 'Rattling bones held together by dark magic.',       35,  8, 3,  25, 8,  1,  6, 0.30, '💀'),
(3,  'Goblin Warrior',  'A green menace armed with a jagged blade.',         45, 10, 4,  30,12,  2,  2, 0.35, '👺'),
(4,  'Cave Troll',      'Massive and dull-witted. Still lethal.',            80, 16, 8,  60,25,  3,  7, 0.25, '👾'),
(5,  'Dark Elf Scout',  'Fast, silent, and ruthless. Do not underestimate.', 60, 18, 7,  70,30,  4,  3, 0.40, '🧝'),
(6,  'Vampire Lord',    'Ancient, aristocratic, hungry. Very hungry.',      100, 22,12, 110,50,  5,  4, 0.30, '🧛'),
(7,  'Stone Golem',     'Animated by forbidden runes. Slow but crushing.',  130, 20,20, 130,60,  6,  8, 0.20, '🗿'),
(8,  'Shadow Demon',    'A being of pure malice from beyond the veil.',     150, 28,15, 160,80,  7, 13, 0.35, '👿'),
(9,  'Dragon Whelp',    'Young, but already a terrifying opponent.',        200, 32,18, 200,100, 8,  9, 0.15, '🐲'),
(10, 'The Lich King',   'Undead sorcerer-king. Destroyer of empires.',      500, 45,25, 500,250,10,  5, 0.50, '💀');

COMMIT;
