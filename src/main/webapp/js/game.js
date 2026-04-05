/**
 * DUNGEON REALM — game.js
 * All AJAX interactions: movement, combat, inventory, chat polling, leaderboard.
 */

/* ── State ──────────────────────────────────────────────── */
let state = {
  inCombat:    false,
  monsterId:   0,
  monsterHp:   0,
  monsterMaxHp:0,
  username:    '',
  playerHp:    100,
  playerMaxHp: 100,
  playerLevel: 1
};

let selectedItemId    = null;
let selectedItemType  = null;
let lastChatId        = 0;
let chatPollInterval  = null;
let lbRefreshInterval = null;

/* ── Shared fetch helper — always sends application/x-www-form-urlencoded ── */
function post(url, params) {
  return fetch(url, {
    method:  'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body:    new URLSearchParams(params).toString()
  }).then(r => r.json());
}

/* ── Init ───────────────────────────────────────────────── */
function initGame() {
  Object.assign(state, window.GAME_STATE || {});
  updateCombatUI();
  loadInventory();
  loadChat(true);
  chatPollInterval  = setInterval(() => loadChat(false), 3000);
  lbRefreshInterval = setInterval(refreshLeaderboard, 30000);
}

/* ═══════════════════════════════════════════════════════════
   MOVEMENT
═══════════════════════════════════════════════════════════ */
async function move(direction) {
  if (state.inCombat) { toast('⚔ Finish the fight first!', 'danger'); return; }

  try {
    const data = await post('game/move', { direction });
    addLogEntry(data.message, data.success ? 'move' : 'system');

    if (data.success) {
      if (data.roomName) document.getElementById('room-name').textContent = data.roomName;
      if (data.exits)    document.getElementById('room-exits').querySelector('.exits-text').textContent = data.exits;
      const badge = document.getElementById('room-badge');
      if (badge && data.roomType) badge.textContent = data.roomType.toUpperCase();
      updatePlayerStats(data);
      if (data.event) handleEvent(data.event);
    }
  } catch (e) {
    addLogEntry('❌ Connection error.', 'system');
  }
}

/* ═══════════════════════════════════════════════════════════
   EXPLORE
═══════════════════════════════════════════════════════════ */
async function explore() {
  if (state.inCombat) { toast('⚔ Finish the fight first!', 'danger'); return; }

  try {
    const data = await post('game/explore', {});
    addLogEntry(data.message || 'You search the area...', 'move');
    updatePlayerStats(data);
    if (data.event) handleEvent(data.event);
  } catch (e) {
    addLogEntry('❌ Connection error.', 'system');
  }
}

/* ═══════════════════════════════════════════════════════════
   EVENT HANDLER
═══════════════════════════════════════════════════════════ */
function handleEvent(ev) {
  if (!ev || !ev.type) return;
  addLogEntry(ev.message, ev.type === 'combat' ? 'combat' : 'loot');

  if (ev.type === 'combat') {
    state.inCombat     = true;
    state.monsterId    = ev.monsterId;
    state.monsterHp    = ev.monsterHp;
    state.monsterMaxHp = ev.monsterMaxHp || ev.monsterHp;
    document.getElementById('monster-icon').textContent = ev.monsterIcon || '👹';
    document.getElementById('monster-name').textContent = ev.monsterName || '???';
    updateMonsterHpBar(state.monsterHp, state.monsterMaxHp);
    updateCombatUI();
  } else if (ev.type === 'trap' || ev.type === 'heal') {
    refreshStats();
  }
}

/* ═══════════════════════════════════════════════════════════
   COMBAT
═══════════════════════════════════════════════════════════ */
async function combatAction(action) {
  try {
    const data = await post('combat', { action });
    addLogEntry(data.message, 'combat');
    updatePlayerStats(data);

    if (data.monsterHp !== undefined) {
      state.monsterHp = data.monsterHp;
      const pct = data.monsterHpPct !== undefined
        ? data.monsterHpPct
        : (state.monsterMaxHp > 0 ? Math.round(state.monsterHp / state.monsterMaxHp * 100) : 0);
      updateMonsterHpBar(state.monsterHp, state.monsterMaxHp, pct);
    }

    if (data.combatOver) {
      state.inCombat  = false;
      state.monsterId = 0;
      if (data.playerWon) {
        if (data.lootItem) toast('✨ Loot: ' + data.lootItem, 'gold');
        if (data.levelled) showLevelUp(data.playerLevel);
      } else {
        toast('💀 You were slain! Returned to village.', 'danger');
      }
      updateCombatUI();
      loadInventory();
    }
  } catch (e) {
    addLogEntry('❌ Connection error.', 'system');
  }
}

function updateCombatUI() {
  const combatPanel   = document.getElementById('combat-panel');
  const combatActions = document.getElementById('combat-actions');
  const moveActions   = document.getElementById('move-actions');

  if (state.inCombat) {
    combatPanel.classList.add('active');
    combatActions.style.display    = 'flex';
    moveActions.style.opacity      = '0.4';
    moveActions.style.pointerEvents = 'none';
  } else {
    combatPanel.classList.remove('active');
    combatActions.style.display    = 'none';
    moveActions.style.opacity      = '1';
    moveActions.style.pointerEvents = '';
  }
}

/* ── Player stat updates ─────────────────────────────────── */
function updatePlayerStats(data) {
  if (data.playerHp    !== undefined) setBar('hp', data.playerHp, data.playerMaxHp || state.playerMaxHp);
  if (data.playerGold  !== undefined) setText('stat-gold', data.playerGold);
  if (data.playerXp    !== undefined) setText('xp-text', data.playerXp + ' / ' + (data.xpNext || ''));
  if (data.playerLevel !== undefined) {
    setText('stat-level', data.playerLevel);
    setText('hud-level',  data.playerLevel);
    state.playerLevel = data.playerLevel;
  }
  if (data.playerHp !== undefined) {
    state.playerHp    = data.playerHp;
    state.playerMaxHp = data.playerMaxHp || state.playerMaxHp;
    setBarById('p-combat-hp', data.playerHp, state.playerMaxHp);
    setText('p-combat-hp-text', data.playerHp + '/' + state.playerMaxHp);
  }
  if (data.playerKills !== undefined) setText('stat-kills', data.playerKills);
}

function setBar(name, cur, max) {
  const bar  = document.getElementById(name + '-bar');
  const text = document.getElementById(name + '-text');
  if (!bar || !max) return;
  bar.style.width = Math.max(0, Math.min(100, Math.round(cur / max * 100))) + '%';
  if (text) text.textContent = cur + ' / ' + max;
}

function setBarById(id, cur, max) {
  const el = document.getElementById(id);
  if (el && max) el.style.width = Math.max(0, Math.min(100, Math.round(cur / max * 100))) + '%';
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

function updateMonsterHpBar(hp, maxHp, pct) {
  const bar  = document.getElementById('m-combat-hp');
  const text = document.getElementById('m-combat-hp-text');
  if (bar) {
    const p = pct !== undefined ? pct : (maxHp > 0 ? Math.max(0, Math.round(hp / maxHp * 100)) : 0);
    bar.style.width = p + '%';
  }
  if (text) text.textContent = hp + ' HP';
}

async function refreshStats() {
  try {
    const data = await fetch('game/state').then(r => r.json());
    setBar('hp', data.hp, data.maxHp);
    setBar('mp', data.mp, data.maxMp);
    setText('stat-gold',  data.gold);
    setText('stat-level', data.level);
    setText('hud-level',  data.level);
    setText('xp-text',    data.xp + ' / ' + data.xpNext);
    setText('stat-atk',   data.attack);
    setText('stat-def',   data.defense);
  } catch (_) {}
}

/* ═══════════════════════════════════════════════════════════
   INVENTORY
═══════════════════════════════════════════════════════════ */
async function loadInventory() {
  try {
    const items = await fetch('inventory').then(r => r.json());
    const list  = document.getElementById('inv-list');
    list.innerHTML = '';

    if (!items.length) {
      list.innerHTML = '<div class="inv-empty">Your pack is empty.</div>';
      return;
    }

    items.forEach(ii => {
      const div = document.createElement('div');
      div.className = 'inv-item rarity-' + (ii.rarity || 'common');
      div.dataset.itemId   = ii.itemId;
      div.dataset.itemType = ii.itemType;
      div.dataset.item     = JSON.stringify(ii);
      div.onclick = () => selectItem(div);
      div.innerHTML =
        '<span class="inv-icon">' + (ii.icon || '📦') + '</span>' +
        '<div class="inv-info">' +
          '<div class="inv-name' + (ii.equipped ? ' equipped' : '') + '">' +
            esc(ii.name) + (ii.equipped ? ' <span class="equipped-tag">E</span>' : '') +
          '</div>' +
          '<div class="inv-type">' + ii.itemType + '</div>' +
        '</div>' +
        '<div class="inv-qty">×' + ii.quantity + '</div>';
      list.appendChild(div);
    });
  } catch (_) {}
}

function selectItem(el) {
  const ii = JSON.parse(el.dataset.item || '{}');
  selectedItemId   = ii.itemId;
  selectedItemType = ii.itemType;

  const detail = document.getElementById('item-detail');
  detail.style.display = 'block';
  document.getElementById('id-name').textContent = ii.name;
  document.getElementById('id-desc').textContent = ii.description;

  let statsStr = '';
  if (ii.attackBonus  > 0) statsStr += '⚔ +' + ii.attackBonus  + ' ATK  ';
  if (ii.defenseBonus > 0) statsStr += '🛡 +' + ii.defenseBonus + ' DEF  ';
  if (ii.hpRestore    > 0) statsStr += '❤ +' + ii.hpRestore    + ' HP  ';
  document.getElementById('id-stats').textContent = statsStr || 'No bonuses.';

  const btnUse   = document.getElementById('btn-use');
  const btnEquip = document.getElementById('btn-equip');
  btnUse.style.display   = ii.itemType === 'consumable' ? '' : 'none';
  btnEquip.style.display = (ii.itemType === 'weapon' || ii.itemType === 'armor') ? '' : 'none';
  btnEquip.textContent   = ii.equipped ? 'Unequip' : 'Equip';
}

function closeItemDetail() {
  document.getElementById('item-detail').style.display = 'none';
  selectedItemId = null;
}

async function useItem() {
  if (!selectedItemId) return;
  try {
    const data = await post('inventory', { action: 'use', itemId: selectedItemId });
    addLogEntry(data.message, 'loot');
    if (data.success) {
      updatePlayerStats(data);
      closeItemDetail();
      loadInventory();
    } else {
      toast(data.message, 'danger');
    }
  } catch (_) {}
}

async function equipItem() {
  if (!selectedItemId) return;
  try {
    const data = await post('inventory', { action: 'equip', itemId: selectedItemId });
    if (data.success) {
      addLogEntry(data.message, 'loot');
      refreshStats();
      closeItemDetail();
      loadInventory();
    } else {
      toast(data.message, 'danger');
    }
  } catch (_) {}
}

/* ═══════════════════════════════════════════════════════════
   GAME LOG
═══════════════════════════════════════════════════════════ */
function addLogEntry(html, type) {
  const log = document.getElementById('game-log');
  if (!log || !html) return;
  const time  = new Date().toTimeString().slice(0, 8);
  const entry = document.createElement('div');
  entry.className = 'log-entry log-' + (type || 'system');
  entry.innerHTML = '<span class="log-time">' + time + '</span><span class="log-msg">' + html + '</span>';
  log.appendChild(entry);
  log.scrollTop = log.scrollHeight;
  while (log.children.length > 80) log.removeChild(log.firstChild);
}

/* ═══════════════════════════════════════════════════════════
   CHAT
═══════════════════════════════════════════════════════════ */
async function loadChat(initial) {
  try {
    const url  = initial ? 'chat?channel=global' : 'chat?channel=global&since=' + lastChatId;
    const msgs = await fetch(url).then(r => r.json());
    if (!msgs.length) return;

    const container = document.getElementById('chat-messages');
    msgs.forEach(m => {
      lastChatId = Math.max(lastChatId, m.id);
      const div  = document.createElement('div');
      div.className = 'chat-msg';
      const isSelf = m.username === state.username;
      div.innerHTML =
        '<span class="chat-time">'     + m.time + '</span>' +
        '<span class="chat-username'   + (isSelf ? ' self' : '') + '">' + esc(m.username) + '</span> ' +
        '<span class="chat-text">'     + esc(m.message) + '</span>';
      container.appendChild(div);
    });
    container.scrollTop = container.scrollHeight;
    while (container.children.length > 60) container.removeChild(container.firstChild);
  } catch (_) {}
}

async function sendChat() {
  const input = document.getElementById('chat-input');
  const msg   = input.value.trim();
  if (!msg) return;
  input.value = '';
  try {
    await post('chat', { message: msg, channel: 'global' });
    loadChat(false);
  } catch (_) {}
}

document.addEventListener('DOMContentLoaded', () => {
  const ci = document.getElementById('chat-input');
  if (ci) ci.addEventListener('keydown', e => { if (e.key === 'Enter') sendChat(); });
});

/* ═══════════════════════════════════════════════════════════
   LEADERBOARD
═══════════════════════════════════════════════════════════ */
async function refreshLeaderboard() {
  try {
    const data  = await fetch('leaderboard').then(r => r.json());
    const tbody = document.querySelector('#lb-table tbody');
    if (!tbody) return;
    tbody.innerHTML = '';
    data.forEach(row => {
      const tr = document.createElement('tr');
      tr.className = 'lb-row' + (row.username === state.username ? ' lb-self' : '');
      tr.innerHTML =
        '<td class="lb-rank">' + row.rank + '</td>' +
        '<td class="lb-user">' + (row.online ? '<span class="online-dot-sm"></span>' : '') + esc(row.username) + '</td>' +
        '<td>' + row.level + '</td>' +
        '<td>' + row.kills + '</td>' +
        '<td class="lb-gold">💰' + row.gold + '</td>';
      tbody.appendChild(tr);
    });
  } catch (_) {}
}

/* ═══════════════════════════════════════════════════════════
   LEVEL UP
═══════════════════════════════════════════════════════════ */
function showLevelUp(level) {
  document.getElementById('levelup-msg').textContent = 'You reached Level ' + level + '! Stats increased!';
  document.getElementById('levelup-overlay').style.display = 'flex';
}

function closeLevelUp() {
  document.getElementById('levelup-overlay').style.display = 'none';
  refreshStats();
}

/* ═══════════════════════════════════════════════════════════
   LOGOUT
═══════════════════════════════════════════════════════════ */
async function logout() {
  try {
    const data = await post('auth', { action: 'logout' });
    window.location.href = data.redirect || '/';
  } catch (_) { window.location.href = '/'; }
}

/* ═══════════════════════════════════════════════════════════
   TOAST
═══════════════════════════════════════════════════════════ */
let toastTimer = null;
function toast(msg, type) {
  const el = document.getElementById('toast');
  if (!el) return;
  el.innerHTML  = msg;
  el.className  = 'toast show ' + (type || '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 3000);
}

/* ── Utility ─────────────────────────────────────────────── */
function esc(s) {
  if (!s) return '';
  return String(s)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
