<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dungeon.model.*,com.dungeon.dao.*,java.util.*" %>
<%
    PlayerStats stats   = (PlayerStats)  request.getAttribute("stats");
    Room        room    = (Room)         request.getAttribute("room");
    List<InventoryItem> inventory   = (List<InventoryItem>) request.getAttribute("inventory");
    List<Map<String,String>> gamelog = (List<Map<String,String>>) request.getAttribute("gamelog");
    List<Map<String,Object>> leaderboard = (List<Map<String,Object>>) request.getAttribute("leaderboard");
    List<Map<String,String>> onlinePlayers = (List<Map<String,String>>) request.getAttribute("onlinePlayers");
    String username = (String) session.getAttribute("username");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Dungeon Realm — <%= username %>'s Quest</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Cinzel+Decorative:wght@700&family=Cinzel:wght@400;600&family=Crimson+Text:ital,wght@0,400;0,600;1,400&family=Share+Tech+Mono&display=swap" rel="stylesheet">
<link rel="stylesheet" href="css/game.css">
</head>
<body>

<!-- ═══ TOP NAVIGATION BAR ═══════════════════════════════════════════ -->
<header class="topbar">
  <div class="topbar-brand">
    <span class="topbar-skull">💀</span>
    <span class="topbar-title">Dungeon Realm</span>
  </div>
  <div class="topbar-player">
    <span class="online-dot"></span>
    <span class="topbar-username"><%= username %></span>
    <span class="topbar-level">LVL <span id="hud-level"><%= stats.getLevel() %></span></span>
    <button class="btn-logout" onclick="logout()">⬡ Exit</button>
  </div>
</header>

<!-- ═══ MAIN LAYOUT GRID ══════════════════════════════════════════════ -->
<main class="game-grid">

  <!-- ──────────── LEFT COLUMN ──────────── -->
  <aside class="left-col">

    <!-- Player Stats Card -->
    <div class="panel stat-panel">
      <div class="panel-title">⚔ Hero Stats</div>

      <div class="stat-avatar">
        <div class="avatar-circle">
          <span class="avatar-emoji">🧙</span>
          <div class="avatar-level">LVL <span id="stat-level"><%= stats.getLevel() %></span></div>
        </div>
        <div class="avatar-info">
          <div class="hero-name"><%= username %></div>
          <div class="hero-class">Shadow Walker</div>
        </div>
      </div>

      <!-- HP Bar -->
      <div class="bar-group">
        <div class="bar-label">
          <span>❤ HP</span>
          <span id="hp-text"><%= stats.getHp() %> / <%= stats.getMaxHp() %></span>
        </div>
        <div class="bar-track">
          <div class="bar-fill hp-fill" id="hp-bar"
               style="width:<%= stats.hpPercent() %>%"></div>
        </div>
      </div>

      <!-- MP Bar -->
      <div class="bar-group">
        <div class="bar-label">
          <span>💧 MP</span>
          <span id="mp-text"><%= stats.getMp() %> / <%= stats.getMaxMp() %></span>
        </div>
        <div class="bar-track">
          <div class="bar-fill mp-fill" id="mp-bar"
               style="width:<%= (stats.getMaxMp()>0?stats.getMp()*100/stats.getMaxMp():0) %>%"></div>
        </div>
      </div>

      <!-- XP Bar -->
      <div class="bar-group">
        <div class="bar-label">
          <span>✨ XP</span>
          <span id="xp-text"><%= stats.getXp() %> / <%= stats.getXpNext() %></span>
        </div>
        <div class="bar-track xp-track">
          <div class="bar-fill xp-fill" id="xp-bar"
               style="width:<%= stats.xpPercent() %>%"></div>
        </div>
      </div>

      <!-- Stat Grid -->
      <div class="stat-grid">
        <div class="stat-chip">
          <span class="chip-icon">⚔</span>
          <span class="chip-val" id="stat-atk"><%= stats.getAttack() %></span>
          <span class="chip-label">ATK</span>
        </div>
        <div class="stat-chip">
          <span class="chip-icon">🛡</span>
          <span class="chip-val" id="stat-def"><%= stats.getDefense() %></span>
          <span class="chip-label">DEF</span>
        </div>
        <div class="stat-chip">
          <span class="chip-icon">💰</span>
          <span class="chip-val" id="stat-gold"><%= stats.getGold() %></span>
          <span class="chip-label">GOLD</span>
        </div>
        <div class="stat-chip">
          <span class="chip-icon">💀</span>
          <span class="chip-val" id="stat-kills"><%= stats.getKills() %></span>
          <span class="chip-label">KILLS</span>
        </div>
      </div>
    </div>

    <!-- Online Players -->
    <div class="panel online-panel">
      <div class="panel-title">🌐 Online Heroes</div>
      <div class="online-list" id="online-list">
        <% if (onlinePlayers != null) { for (Map<String,String> p : onlinePlayers) { %>
        <div class="online-item">
          <span class="online-dot-sm"></span>
          <span class="online-name"><%= p.get("username") %></span>
          <span class="online-lvl">Lv<%= p.get("level") %></span>
        </div>
        <% }} %>
      </div>
    </div>

  </aside>

  <!-- ──────────── CENTRE COLUMN ──────────── -->
  <section class="centre-col">

    <!-- Room Display -->
    <div class="panel room-panel" id="room-panel">
      <div class="room-header">
        <div class="room-type-badge" id="room-badge"><%= room.getRoomType().toUpperCase() %></div>
        <div class="room-danger">
          <% for(int d=0;d<5;d++) { %>
          <span class="danger-pip <%= d < room.getDangerLevel() ? "active" : "" %>">◆</span>
          <% } %>
        </div>
      </div>
      <div class="room-name" id="room-name"><%= room.getName() %></div>
      <div class="room-desc" id="room-desc"><%= room.getDescription() %></div>
      <div class="room-exits" id="room-exits">
        Exits: <span class="exits-text"><%= room.getExits() %></span>
      </div>
    </div>

    <!-- Combat Arena (hidden unless in combat) -->
    <div class="panel combat-panel <%= stats.isInCombat() ? "active" : "" %>" id="combat-panel">
      <div class="combat-title">⚔ BATTLE</div>
      <div class="combat-arena">
        <div class="combatant player-side">
          <div class="combatant-icon">🧙</div>
          <div class="combatant-name"><%= username %></div>
          <div class="bar-track">
            <div class="bar-fill hp-fill" id="p-combat-hp"
                 style="width:<%= stats.hpPercent() %>%"></div>
          </div>
          <div class="combatant-hp-text" id="p-combat-hp-text"><%= stats.getHp() %>/<%= stats.getMaxHp() %></div>
        </div>

        <div class="vs-badge">VS</div>

        <div class="combatant monster-side" id="monster-side">
          <div class="combatant-icon" id="monster-icon">👹</div>
          <div class="combatant-name" id="monster-name">???</div>
          <div class="bar-track">
            <div class="bar-fill monster-fill" id="m-combat-hp" style="width:100%"></div>
          </div>
          <div class="combatant-hp-text" id="m-combat-hp-text">??? HP</div>
        </div>
      </div>
    </div>

    <!-- Game Log -->
    <div class="panel log-panel">
      <div class="panel-title">📜 Chronicle</div>
      <div class="game-log" id="game-log">
        <% if (gamelog != null) { for (Map<String,String> entry : gamelog) { %>
        <div class="log-entry log-<%= entry.get("type") %>">
          <span class="log-time"><%= entry.get("time") != null ? entry.get("time").substring(11,19) : "" %></span>
          <span class="log-msg"><%= entry.get("message") %></span>
        </div>
        <% }} %>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="action-bar" id="action-bar">
      <!-- Movement -->
      <div class="action-group" id="move-actions">
        <div class="action-group-label">Movement</div>
        <div class="move-pad">
          <div></div>
          <button class="btn-action btn-move" onclick="move('north')" id="btn-north">↑ N</button>
          <div></div>
          <button class="btn-action btn-move" onclick="move('west')"  id="btn-west">← W</button>
          <button class="btn-action btn-explore" onclick="explore()">🔍</button>
          <button class="btn-action btn-move" onclick="move('east')"  id="btn-east">E →</button>
          <div></div>
          <button class="btn-action btn-move" onclick="move('south')" id="btn-south">↓ S</button>
          <div></div>
        </div>
      </div>

      <!-- Combat actions -->
      <div class="action-group" id="combat-actions" style="display:<%= stats.isInCombat()?"flex":"none" %>">
        <div class="action-group-label">Combat</div>
        <button class="btn-action btn-attack" onclick="combatAction('attack')">⚔ Attack</button>
        <button class="btn-action btn-defend" onclick="combatAction('defend')">🛡 Defend</button>
        <button class="btn-action btn-flee"   onclick="combatAction('flee')">💨 Flee</button>
      </div>
    </div>

    <!-- Notification Toast -->
    <div class="toast" id="toast"></div>

  </section>

  <!-- ──────────── RIGHT COLUMN ──────────── -->
  <aside class="right-col">

    <!-- Inventory -->
    <div class="panel inv-panel">
      <div class="panel-title">🎒 Inventory</div>
      <div class="inv-list" id="inv-list">
        <% if (inventory != null) { for (InventoryItem ii : inventory) { %>
        <div class="inv-item rarity-<%= ii.getRarity() %>"
             data-item-id="<%= ii.getItemId() %>"
             data-item-type="<%= ii.getItemType() %>"
             onclick="selectItem(this)">
          <span class="inv-icon"><%= ii.getIcon() %></span>
          <div class="inv-info">
            <div class="inv-name <%= ii.isEquipped() ? "equipped" : "" %>">
              <%= ii.getName() %>
              <% if (ii.isEquipped()) { %><span class="equipped-tag">E</span><% } %>
            </div>
            <div class="inv-type"><%= ii.getItemType() %></div>
          </div>
          <div class="inv-qty">×<%= ii.getQuantity() %></div>
        </div>
        <% }} %>
        <% if (inventory == null || inventory.isEmpty()) { %>
        <div class="inv-empty">Your pack is empty.</div>
        <% } %>
      </div>

      <!-- Item detail popup -->
      <div class="item-detail" id="item-detail" style="display:none">
        <div class="item-detail-name" id="id-name"></div>
        <div class="item-detail-desc" id="id-desc"></div>
        <div class="item-detail-stats" id="id-stats"></div>
        <div class="item-detail-btns">
          <button class="btn-use"   id="btn-use"   onclick="useItem()">Use</button>
          <button class="btn-equip" id="btn-equip"  onclick="equipItem()">Equip / Unequip</button>
          <button class="btn-close-detail" onclick="closeItemDetail()">✕</button>
        </div>
      </div>
    </div>

    <!-- Leaderboard -->
    <div class="panel lb-panel">
      <div class="panel-title">🏆 Leaderboard</div>
      <div class="lb-table-wrap">
        <table class="lb-table" id="lb-table">
          <thead>
            <tr><th>#</th><th>Hero</th><th>Lv</th><th>Kills</th><th></th></tr>
          </thead>
          <tbody>
            <% if (leaderboard != null) { int rank=1; for (Map<String,Object> row : leaderboard) { %>
            <tr class="lb-row <%= (String.valueOf(row.get("username")).equals(username))?"lb-self":"" %>">
              <td class="lb-rank"><%= row.get("rank") %></td>
              <td class="lb-user">
                <% if ((Boolean)row.get("online")) { %><span class="online-dot-sm"></span><% } %>
                <%= row.get("username") %>
              </td>
              <td><%= row.get("level") %></td>
              <td><%= row.get("kills") %></td>
              <td class="lb-gold">💰<%= row.get("gold") %></td>
            </tr>
            <% rank++; }} %>
          </tbody>
        </table>
      </div>
    </div>

  </aside>

</main>

<!-- ═══ GLOBAL CHAT BAR ════════════════════════════════════════════════ -->
<div class="chat-bar">
  <div class="chat-messages" id="chat-messages">
    <!-- populated by JS -->
  </div>
  <div class="chat-input-row">
    <span class="chat-channel">#global</span>
    <input type="text" id="chat-input" class="chat-input"
           placeholder="Say something brave..." maxlength="200">
    <button class="chat-send" onclick="sendChat()">Send</button>
  </div>
</div>

<!-- Level-Up Overlay -->
<div class="levelup-overlay" id="levelup-overlay" style="display:none">
  <div class="levelup-card">
    <div class="levelup-icon">⭐</div>
    <div class="levelup-title">LEVEL UP!</div>
    <div class="levelup-msg" id="levelup-msg">You reached level X!</div>
    <button class="btn-primary-sm" onclick="closeLevelUp()">Continue</button>
  </div>
</div>

<script src="js/game.js"></script>
<script>
  // Seed JS state from server
  window.GAME_STATE = {
    inCombat:    <%= stats.isInCombat() %>,
    monsterId:   <%= stats.getCurrentMonsterId() != null ? stats.getCurrentMonsterId() : 0 %>,
    monsterHp:   <%= (Integer)(session.getAttribute("monsterHp") != null ? session.getAttribute("monsterHp") : 0) %>,
    monsterMaxHp:<%= (Integer)(session.getAttribute("monsterMaxHp") != null ? session.getAttribute("monsterMaxHp") : 0) %>,
    username:    '<%= username %>',
    playerHp:    <%= stats.getHp() %>,
    playerMaxHp: <%= stats.getMaxHp() %>,
    playerLevel: <%= stats.getLevel() %>
  };
  initGame();
</script>
</body>
</html>
