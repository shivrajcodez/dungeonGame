package com.dungeon.servlet;
import com.dungeon.dao.*;
import com.dungeon.engine.GameEngine;
import com.dungeon.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(name="GameServlet",
    urlPatterns={"/game","/game/move","/game/explore","/game/state"})
public class GameServlet extends HttpServlet {
    private final UserDAO       userDAO      = new UserDAO();
    private final RoomDAO       roomDAO      = new RoomDAO();
    private final InventoryDAO  inventoryDAO = new InventoryDAO();
    private final GameLogDAO    logDAO       = new GameLogDAO();
    private final LeaderboardDAO lbDAO       = new LeaderboardDAO();
    private final GameEngine    engine       = new GameEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/"); return;
        }
        int userId = (int) session.getAttribute("userId");
        String path = req.getServletPath();
        try {
            if ("/game/state".equals(path)) { sendState(resp, userId); return; }
            PlayerStats stats = userDAO.loadStats(userId);
            Room room = roomDAO.findById(stats.getCurrentRoom());
            req.setAttribute("stats",         stats);
            req.setAttribute("room",          room);
            req.setAttribute("inventory",     inventoryDAO.findByUser(userId));
            req.setAttribute("gamelog",       logDAO.getRecent(userId, 30));
            req.setAttribute("leaderboard",   lbDAO.getTop(10));
            req.setAttribute("onlinePlayers", lbDAO.getOnlinePlayers());
            req.getRequestDispatcher("/WEB-INF/jsp/game.jsp").forward(req, resp);
        } catch (Exception e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json;charset=UTF-8");
        if (session == null || session.getAttribute("userId") == null) {
            resp.getWriter().write("{"success":false,"message":"Not logged in."}"); return;
        }
        int userId = (int) session.getAttribute("userId");
        String path = req.getServletPath();
        try {
            Map<String,Object> result;
            if ("/game/move".equals(path)) {
                result = engine.move(userId, req.getParameter("direction"));
            } else if ("/game/explore".equals(path)) {
                result = engine.explore(userId);
                Object ev = result.get("event");
                if (ev instanceof Map<?,?> evMap && "combat".equals(evMap.get("type"))) {
                    session.setAttribute("monsterHp",    evMap.get("monsterHp"));
                    session.setAttribute("monsterMaxHp", evMap.get("monsterMaxHp"));
                }
            } else { result = Map.of("success",false,"message","Unknown endpoint"); }
            PlayerStats stats = userDAO.loadStats(userId);
            result = new LinkedHashMap<>(result);
            result.put("playerHp",    stats.getHp());
            result.put("playerMaxHp", stats.getMaxHp());
            result.put("playerGold",  stats.getGold());
            result.put("playerXp",    stats.getXp());
            result.put("playerLevel", stats.getLevel());
            resp.getWriter().write(toJson(result));
        } catch (Exception e) {
            resp.getWriter().write("{"success":false,"message":"" + esc(e.getMessage()) + ""}");
        }
    }

    private void sendState(HttpServletResponse resp, int userId) throws Exception {
        PlayerStats s = userDAO.loadStats(userId);
        Room r = roomDAO.findById(s.getCurrentRoom());
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("hp",s.getHp()); m.put("maxHp",s.getMaxHp()); m.put("mp",s.getMp());
        m.put("maxMp",s.getMaxMp()); m.put("level",s.getLevel()); m.put("xp",s.getXp());
        m.put("xpNext",s.getXpNext()); m.put("gold",s.getGold());
        m.put("attack",s.getAttack()); m.put("defense",s.getDefense());
        m.put("kills",s.getKills()); m.put("deaths",s.getDeaths());
        m.put("roomName",r.getName()); m.put("exits",r.getExits());
        m.put("inCombat",s.isInCombat());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(toJson(m));
    }

    @SuppressWarnings("unchecked")
    public static String toJson(Map<String,Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String,Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Boolean || v instanceof Number) sb.append(v);
            else if (v instanceof Map) sb.append(toJson((Map<String,Object>)v));
            else sb.append("\"").append(esc(v.toString())).append("\"");
        }
        return sb.append("}").toString();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\"").replace("\n","\\n").replace("\r","");
    }
}
