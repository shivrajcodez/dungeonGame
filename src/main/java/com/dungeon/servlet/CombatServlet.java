package com.dungeon.servlet;
import com.dungeon.dao.MonsterDAO;
import com.dungeon.dao.UserDAO;
import com.dungeon.engine.GameEngine;
import com.dungeon.model.PlayerStats;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(name="CombatServlet", urlPatterns={"/combat"})
public class CombatServlet extends HttpServlet {
    private final UserDAO    userDAO  = new UserDAO();
    private final MonsterDAO monDAO   = new MonsterDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json;charset=UTF-8");
        if (session == null || session.getAttribute("userId") == null) {
            resp.getWriter().write("{"success":false,"message":"Not logged in."}"); return;
        }
        int userId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");
        try {
            Integer monHp = (Integer) session.getAttribute("monsterHp");
            if (monHp == null) {
                PlayerStats s = userDAO.loadStats(userId);
                monHp = resolveMonsterHp(s);
            }
            final int currentMonHp = monHp;
            GameEngine eng = new GameEngine() {
                @Override public int getSessionMonsterHp(int uid) { return currentMonHp; }
            };

            Map<String,Object> result = switch (action == null ? "" : action) {
                case "attack" -> eng.attack(userId);
                case "defend" -> eng.defend(userId);
                case "flee"   -> { var r = eng.flee(userId);
                    if (Boolean.TRUE.equals(r.get("success"))) {
                        session.removeAttribute("monsterHp");
                        session.removeAttribute("monsterMaxHp");
                    }
                    yield r; }
                default -> Map.of("success",false,"message","Unknown action");
            };

            result = new LinkedHashMap<>(result);
            Object newHp = result.get("monsterHp");
            if (newHp instanceof Number n) session.setAttribute("monsterHp", n.intValue());

            PlayerStats stats = userDAO.loadStats(userId);
            result.put("playerHp",    stats.getHp());
            result.put("playerMaxHp", stats.getMaxHp());
            result.put("playerHpPct", stats.hpPercent());
            result.put("playerGold",  stats.getGold());
            result.put("playerXp",    stats.getXp());
            result.put("playerLevel", stats.getLevel());
            result.put("playerKills", stats.getKills());

            Integer mHp    = (Integer) session.getAttribute("monsterHp");
            Integer mMaxHp = (Integer) session.getAttribute("monsterMaxHp");
            if (mHp != null && mMaxHp != null && mMaxHp > 0)
                result.put("monsterHpPct", (int)((double)mHp / mMaxHp * 100));

            resp.getWriter().write(GameServlet.toJson(result));
        } catch (Exception e) {
            resp.getWriter().write("{"success":false,"message":"" + e.getMessage() + ""}");
        }
    }

    private int resolveMonsterHp(PlayerStats s) {
        if (s == null || !s.isInCombat() || s.getCurrentMonsterId() == null) return 0;
        try { return monDAO.findById(s.getCurrentMonsterId()).getHp(); }
        catch (Exception e) { return 30; }
    }
}
