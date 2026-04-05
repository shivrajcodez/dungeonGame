package com.dungeon.engine;

import com.dungeon.dao.*;
import com.dungeon.model.*;

import java.sql.SQLException;
import java.util.*;

/**
 * Core game engine: exploration, random events, combat resolution, levelling.
 */
public class GameEngine {

    private static final Random RNG = new Random();

    private final UserDAO      userDAO      = new UserDAO();
    private final RoomDAO      roomDAO      = new RoomDAO();
    private final MonsterDAO   monsterDAO   = new MonsterDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ItemDAO      itemDAO      = new ItemDAO();
    private final GameLogDAO   logDAO       = new GameLogDAO();

    // ── MOVEMENT ─────────────────────────────────────────────
    public Map<String, Object> move(int userId, String direction) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PlayerStats stats = userDAO.loadStats(userId);

        if (stats.isInCombat()) {
            result.put("success", false);
            result.put("message", "You cannot flee while in combat! Fight or die.");
            return result;
        }

        Room current = roomDAO.findById(stats.getCurrentRoom());
        Integer nextRoomId = null;
        String  dir        = direction == null ? "" : direction.toLowerCase();
        if      ("north".equals(dir)) nextRoomId = current.getNorthId();
        else if ("south".equals(dir)) nextRoomId = current.getSouthId();
        else if ("east" .equals(dir)) nextRoomId = current.getEastId();
        else if ("west" .equals(dir)) nextRoomId = current.getWestId();

        if (nextRoomId == null) {
            result.put("success", false);
            result.put("message", "There is no passage to the " + direction + ".");
            return result;
        }

        Room next = roomDAO.findById(nextRoomId);
        stats.setCurrentRoom(nextRoomId);
        userDAO.saveStats(stats);

        logDAO.log(userId, "move", "Moved " + direction + " to " + next.getName());

        result.put("success",     true);
        result.put("message",     "You travel " + direction + " and enter: <strong>"
                                  + next.getName() + "</strong>. " + next.getDescription());
        result.put("roomName",    next.getName());
        result.put("exits",       next.getExits());
        result.put("roomType",    next.getRoomType());
        result.put("dangerLevel", next.getDangerLevel());

        if (!"safe".equals(next.getRoomType()) && !"town".equals(next.getRoomType())) {
            Map<String, Object> event = triggerRandomEvent(userId, stats, next);
            result.put("event", event);
        }
        return result;
    }

    // ── RANDOM EVENTS ────────────────────────────────────────
    private Map<String, Object> triggerRandomEvent(int userId, PlayerStats stats, Room room)
            throws SQLException {
        Map<String, Object> ev = new LinkedHashMap<>();
        double roll = RNG.nextDouble();

        if (roll < 0.40) {
            List<Monster> pool = monsterDAO.findByLevel(stats.getLevel());
            if (pool.isEmpty()) {
                ev.put("type", "quiet");
                ev.put("message", "The dungeon is eerily silent.");
                return ev;
            }
            int maxIdx = Math.min(pool.size(), room.getDangerLevel() * 2 + 2);
            Monster monster = pool.get(RNG.nextInt(Math.min(maxIdx, pool.size())));

            stats.setInCombat(true);
            stats.setCurrentMonsterId(monster.getId());
            userDAO.saveStats(stats);

            ev.put("type",         "combat");
            ev.put("message",      "⚔️  A " + monster.getIcon() + " <strong>"
                                   + monster.getName() + "</strong> emerges from the shadows!");
            ev.put("monsterId",    monster.getId());
            ev.put("monsterName",  monster.getName());
            ev.put("monsterHp",    monster.getHp());
            ev.put("monsterMaxHp", monster.getHp());
            ev.put("monsterIcon",  monster.getIcon());
            logDAO.log(userId, "combat", "Encountered " + monster.getName());

        } else if (roll < 0.55) {
            int gold = RNG.nextInt(20 * room.getDangerLevel()) + 5;
            stats.setGold(stats.getGold() + gold);
            userDAO.saveStats(stats);
            ev.put("type",    "treasure");
            ev.put("message", "💰 You find a hidden stash! <span class='gold'>+" + gold + " gold</span>.");
            logDAO.log(userId, "loot", "Found " + gold + " gold");

        } else if (roll < 0.65) {
            int dmg = RNG.nextInt(10 * room.getDangerLevel()) + 5;
            stats.setHp(stats.getHp() - dmg);
            userDAO.saveStats(stats);
            String[] traps = {"a spike pit", "a poison dart trap", "a falling boulder", "an arcane rune"};
            ev.put("type",    "trap");
            ev.put("message", "⚠️ You trigger " + traps[RNG.nextInt(traps.length)]
                              + "! <span class='danger'>-" + dmg + " HP</span>.");
            logDAO.log(userId, "combat", "Triggered trap: -" + dmg + " HP");

        } else if (roll < 0.72) {
            int heal = RNG.nextInt(20) + 10;
            stats.setHp(Math.min(stats.getMaxHp(), stats.getHp() + heal));
            userDAO.saveStats(stats);
            ev.put("type",    "heal");
            ev.put("message", "✨ A healing spring! <span class='xp'>+" + heal + " HP</span> restored.");

        } else {
            String[] msgs = {
                "The dungeon is eerily silent here.",
                "Shadows dance on the torch-lit walls.",
                "You hear distant dripping water.",
                "A cold wind passes through — and is gone.",
                "Bones crunch softly underfoot."
            };
            ev.put("type",    "quiet");
            ev.put("message", msgs[RNG.nextInt(msgs.length)]);
        }
        return ev;
    }

    // ── COMBAT: ATTACK ────────────────────────────────────────
    public Map<String, Object> attack(int userId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PlayerStats stats = userDAO.loadStats(userId);

        if (!stats.isInCombat() || stats.getCurrentMonsterId() == null) {
            result.put("success", false);
            result.put("message", "You are not in combat.");
            return result;
        }

        Monster monster = monsterDAO.findById(stats.getCurrentMonsterId());
        if (monster == null) {
            stats.setInCombat(false);
            stats.setCurrentMonsterId(null);
            userDAO.saveStats(stats);
            result.put("success", false);
            result.put("message", "Your foe has vanished.");
            return result;
        }

        int monsterHp   = getSessionMonsterHp(userId);
        int playerAtk   = stats.getAttack() + getEquipBonus(userId, "weapon");
        int pDmg        = Math.max(1, playerAtk - monster.getDefense() + RNG.nextInt(6) - 2);
        monsterHp      -= pDmg;

        StringBuilder log = new StringBuilder();
        log.append("⚔️ You strike <strong>").append(monster.getName())
           .append("</strong> for <span class='danger'>").append(pDmg).append(" dmg</span>. ");

        result.put("success", true);

        if (monsterHp <= 0) {
            result.putAll(handleVictory(userId, stats, monster, log));
            result.put("monsterHp",  0);
            result.put("combatOver", true);
            result.put("playerWon",  true);
        } else {
            int playerDef = stats.getDefense() + getEquipBonus(userId, "armor");
            int mDmg      = Math.max(1, monster.getAttack() - playerDef + RNG.nextInt(4) - 1);
            stats.setHp(stats.getHp() - mDmg);
            log.append(monster.getIcon()).append(" ").append(monster.getName())
               .append(" hits back for <span class='danger'>").append(mDmg).append(" dmg</span>.");

            userDAO.saveStats(stats);
            result.put("monsterHp",  monsterHp);
            result.put("combatOver", stats.getHp() <= 0);
            result.put("playerWon",  false);

            if (stats.getHp() <= 0) {
                handleDeath(userId, stats);
                log.append(" <strong class='danger'>You have been slain!</strong>");
            }
        }

        result.put("message", log.toString());
        logDAO.log(userId, "combat", "Attacked " + monster.getName());
        return result;
    }

    // ── COMBAT: DEFEND ────────────────────────────────────────
    public Map<String, Object> defend(int userId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PlayerStats stats = userDAO.loadStats(userId);

        if (!stats.isInCombat()) {
            result.put("success", false);
            result.put("message", "You are not in combat.");
            return result;
        }

        Monster monster  = monsterDAO.findById(stats.getCurrentMonsterId());
        int playerDef    = stats.getDefense() + getEquipBonus(userId, "armor") + 5;
        int mDmg         = Math.max(0, monster.getAttack() - playerDef + RNG.nextInt(3));
        stats.setHp(stats.getHp() - mDmg);
        userDAO.saveStats(stats);

        result.put("success",    true);
        result.put("message",    "🛡️ You brace! " + monster.getIcon() + " " + monster.getName()
                                 + " deals only <span class='danger'>" + mDmg + " dmg</span>.");
        result.put("combatOver", stats.getHp() <= 0);
        result.put("playerWon",  false);
        logDAO.log(userId, "combat", "Defended vs " + monster.getName());
        return result;
    }

    // ── COMBAT: FLEE ──────────────────────────────────────────
    public Map<String, Object> flee(int userId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PlayerStats stats = userDAO.loadStats(userId);

        if (!stats.isInCombat()) {
            result.put("success", false);
            result.put("message", "You are not in combat.");
            return result;
        }

        Monster monster = monsterDAO.findById(stats.getCurrentMonsterId());
        boolean fled    = RNG.nextDouble() > 0.40;

        if (fled) {
            stats.setInCombat(false);
            stats.setCurrentMonsterId(null);
            stats.setCurrentRoom(1);
            userDAO.saveStats(stats);
            result.put("success",    true);
            result.put("message",    "💨 You escape from the " + monster.getName() + "!");
            result.put("combatOver", true);
            logDAO.log(userId, "combat", "Fled from " + monster.getName());
        } else {
            int mDmg = Math.max(1, monster.getAttack() / 2);
            stats.setHp(stats.getHp() - mDmg);
            userDAO.saveStats(stats);
            result.put("success",    false);
            result.put("message",    "❌ Escape failed! " + monster.getIcon() + " " + monster.getName()
                                     + " strikes for <span class='danger'>" + mDmg + " dmg</span>.");
            result.put("combatOver", false);
            result.put("playerHp",   stats.getHp());
        }
        return result;
    }

    // ── USE ITEM ──────────────────────────────────────────────
    public Map<String, Object> useItem(int userId, int itemId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        InventoryItem ii = inventoryDAO.findUserItem(userId, itemId);

        if (ii == null || ii.getQuantity() <= 0) {
            result.put("success", false);
            result.put("message", "You don't have that item.");
            return result;
        }
        if (!"consumable".equals(ii.getItemType())) {
            result.put("success", false);
            result.put("message", "You can't use " + ii.getName() + " directly. Try equipping it.");
            return result;
        }

        PlayerStats stats = userDAO.loadStats(userId);
        int hpGain = ii.getHpRestore();
        int mpGain = ii.getMpRestore();

        if (hpGain > 0) stats.setHp(Math.min(stats.getMaxHp(), stats.getHp() + hpGain));
        if (mpGain > 0) stats.setMp(Math.min(stats.getMaxMp(), stats.getMp() + mpGain));

        inventoryDAO.consumeItem(userId, itemId);
        userDAO.saveStats(stats);

        result.put("success",   true);
        result.put("message",   "🧪 Used <strong>" + ii.getName() + "</strong>. "
                                + (hpGain > 0 ? "<span class='xp'>+" + hpGain + " HP</span> " : "")
                                + (mpGain > 0 ? "<span class='xp'>+" + mpGain + " MP</span>" : ""));
        result.put("playerHp",  stats.getHp());
        result.put("playerMp",  stats.getMp());
        result.put("playerMaxHp", stats.getMaxHp());
        logDAO.log(userId, "loot", "Used " + ii.getName());
        return result;
    }

    // ── EXPLORE ───────────────────────────────────────────────
    public Map<String, Object> explore(int userId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        PlayerStats stats = userDAO.loadStats(userId);

        if (stats.isInCombat()) {
            result.put("success", false);
            result.put("message", "You can't explore while in combat!");
            return result;
        }
        Room room = roomDAO.findById(stats.getCurrentRoom());
        result.put("success", true);
        result.put("message", "You search the area...");

        if (!"safe".equals(room.getRoomType()) && !"town".equals(room.getRoomType())) {
            result.put("event", triggerRandomEvent(userId, stats, room));
        } else {
            result.put("message", "This area feels safe. Nothing to find here.");
        }
        return result;
    }

    // ── HELPERS ───────────────────────────────────────────────
    private Map<String, Object> handleVictory(int userId, PlayerStats stats, Monster monster,
                                               StringBuilder log) throws SQLException {
        Map<String, Object> r = new LinkedHashMap<>();
        stats.setInCombat(false);
        stats.setCurrentMonsterId(null);
        stats.setKills(stats.getKills() + 1);
        stats.setGold(stats.getGold() + monster.getGoldReward());
        stats.setXp(stats.getXp() + monster.getXpReward());

        log.append(" 🏆 <span class='xp'>+").append(monster.getXpReward()).append(" XP</span>")
           .append(" 💰 <span class='gold'>+").append(monster.getGoldReward()).append(" gold</span>.");

        if (RNG.nextDouble() < monster.getLootChance() && monster.getLootItemId() != null) {
            Item item = itemDAO.findById(monster.getLootItemId());
            if (item != null) {
                inventoryDAO.addItem(userId, item.getId(), 1);
                log.append(" ✨ Loot: <span class='rare'>").append(item.getIcon())
                   .append(" ").append(item.getName()).append("</span>!");
                r.put("lootItem", item.getName());
                logDAO.log(userId, "loot", "Looted " + item.getName() + " from " + monster.getName());
            }
        }

        boolean levelled = false;
        while (stats.getXp() >= stats.getXpNext()) {
            levelled = true;
            levelUp(stats);
            log.append(" 🎉 <strong class='rare'>LEVEL UP! Now level ").append(stats.getLevel()).append("!</strong>");
            logDAO.log(userId, "level_up", "Reached level " + stats.getLevel());
        }

        userDAO.saveStats(stats);
        r.put("levelled", levelled);
        return r;
    }

    private void levelUp(PlayerStats s) {
        s.setLevel(s.getLevel() + 1);
        s.setMaxHp(s.getMaxHp() + 15);
        s.setHp(s.getMaxHp());
        s.setMaxMp(s.getMaxMp() + 10);
        s.setMp(s.getMaxMp());
        s.setAttack(s.getAttack() + 3);
        s.setDefense(s.getDefense() + 2);
        s.setXpNext(PlayerStats.xpForLevel(s.getLevel() + 1));
    }

    private void handleDeath(int userId, PlayerStats stats) throws SQLException {
        stats.setInCombat(false);
        stats.setCurrentMonsterId(null);
        stats.setDeaths(stats.getDeaths() + 1);
        stats.setHp(stats.getMaxHp() / 2);
        stats.setCurrentRoom(1);
        int penalty = stats.getGold() / 4;
        stats.setGold(stats.getGold() - penalty);
        userDAO.saveStats(stats);
        logDAO.log(userId, "death", "Slain in battle. Lost " + penalty + " gold.");
    }

    private int getEquipBonus(int userId, String type) throws SQLException {
        List<InventoryItem> inv = inventoryDAO.findByUser(userId);
        int bonus = 0;
        for (InventoryItem ii : inv) {
            if (ii.isEquipped() && type.equals(ii.getItemType())) {
                bonus += "weapon".equals(type) ? ii.getAttackBonus() : ii.getDefenseBonus();
            }
        }
        return bonus;
    }

    /** Overridden by CombatServlet to inject session monster HP. */
    public int getSessionMonsterHp(int userId) { return 0; }
}
