# Dungeon Realm — Multiplayer Text RPG

Live at: **https://dungeonrealm-production.up.railway.app**

A production multiplayer dungeon crawler: Core Java + Servlets + JSP + JDBC + MySQL.
Deployed on Railway with Docker. Auto-seeds its own database on first boot.

---

## Run Locally (Docker — easiest)

```bash
git clone https://github.com/shivrajcodez/dungeonGame
cd dungeonGame
docker compose up --build
# Open http://localhost:8080
```

MySQL + Tomcat start together. Schema loads automatically.

## Run Locally (Manual)

Requirements: Java 17, Maven 3.8+, MySQL 8+, Tomcat 10.1+

```bash
# 1. Start MySQL and create database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS dungeon_realm;"

# 2. Build
mvn clean package

# 3. Deploy WAR
cp target/dungeon-realm.war $TOMCAT_HOME/webapps/ROOT.war
$TOMCAT_HOME/bin/startup.sh

# Open http://localhost:8080
```

The app auto-runs the schema SQL on first boot — no manual SQL import needed.

---

## Architecture

```
src/main/java/com/dungeon/
├── servlet/     AuthServlet, GameServlet, CombatServlet,
│                InventoryServlet, ChatServlet, LeaderboardServlet
├── engine/      GameEngine  (combat, events, levelling)
├── model/       User, PlayerStats, Room, Item, Monster, InventoryItem, ChatMessage
├── dao/         UserDAO, RoomDAO, MonsterDAO, ItemDAO,
│                InventoryDAO, GameLogDAO, ChatDAO, LeaderboardDAO
└── util/        DBConnection (env-aware), PasswordUtil, SchemaInitializer

src/main/webapp/
├── WEB-INF/jsp/ index.jsp (login/register), game.jsp (HUD), error.jsp
├── WEB-INF/sql/ schema.sql  ← auto-run on startup
├── css/game.css
└── js/game.js
```

## Database (8 tables + 1 view)

- users, player_stats, rooms (self-referential exits), items, inventory
- monsters, game_log, chat_messages
- VIEW leaderboard (RANK() OVER window function)

## Railway Deployment

1. Fork / push to GitHub
2. New Railway project → deploy from GitHub repo
3. Add MySQL service → Railway auto-injects MYSQLHOST/PORT/DATABASE/PASSWORD
4. Push to main → auto-deploys

No manual SQL step — SchemaInitializer runs schema.sql automatically on first boot.

## Gameplay

| Action | How |
|--------|-----|
| Move | N/S/E/W buttons |
| Combat | Auto-triggers in dungeon rooms |
| Attack / Defend / Flee | Combat panel |
| Use potion | Inventory panel |
| Equip gear | Inventory panel |
| Chat | Bottom bar |

Death respawns at village, costs 25% gold.
Level up boosts HP, MP, ATK, DEF. Boss fight at Room 10.
