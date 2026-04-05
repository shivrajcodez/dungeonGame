package com.dungeon.servlet;
import com.dungeon.dao.InventoryDAO;
import com.dungeon.dao.UserDAO;
import com.dungeon.engine.GameEngine;
import com.dungeon.model.InventoryItem;
import com.dungeon.model.PlayerStats;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(name="InventoryServlet", urlPatterns={"/inventory"})
public class InventoryServlet extends HttpServlet {
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final UserDAO      userDAO      = new UserDAO();
    private final GameEngine   engine       = new GameEngine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json;charset=UTF-8");
        if (session == null || session.getAttribute("userId") == null) {
            resp.getWriter().write("[]"); return;
        }
        int userId = (int) session.getAttribute("userId");
        try {
            List<InventoryItem> items = inventoryDAO.findByUser(userId);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < items.size(); i++) {
                InventoryItem ii = items.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"inventoryId\":").append(ii.getInventoryId()).append(",")
                    .append("\"itemId\":").append(ii.getItemId()).append(",")
                    .append("\"name\":\"").append(esc(ii.getName())).append("\",")
                    .append("\"description\":\"").append(esc(ii.getDescription())).append("\",")
                    .append("\"itemType\":\"").append(ii.getItemType()).append("\",")
                    .append("\"quantity\":").append(ii.getQuantity()).append(",")
                    .append("\"equipped\":").append(ii.isEquipped()).append(",")
                    .append("\"attackBonus\":").append(ii.getAttackBonus()).append(",")
                    .append("\"defenseBonus\":").append(ii.getDefenseBonus()).append(",")
                    .append("\"hpRestore\":").append(ii.getHpRestore()).append(",")
                    .append("\"rarity\":\"").append(ii.getRarity()).append("\",")
                    .append("\"icon\":\"").append(esc(ii.getIcon())).append("\"")
                    .append("}");
            }
            resp.getWriter().write(json.append("]").toString());
        } catch (Exception e) { resp.getWriter().write("[]"); }
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
        String action = req.getParameter("action");
        String itemIdStr = req.getParameter("itemId");
        try {
            int itemId = Integer.parseInt(itemIdStr);
            Map<String,Object> result;
            if ("use".equals(action)) {
                result = engine.useItem(userId, itemId);
            } else if ("equip".equals(action)) {
                InventoryItem ii = inventoryDAO.findUserItem(userId, itemId);
                if (ii == null) {
                    resp.getWriter().write("{"success":false,"message":"Item not found."}"); return;
                }
                if (!"weapon".equals(ii.getItemType()) && !"armor".equals(ii.getItemType())) {
                    resp.getWriter().write("{"success":false,"message":"Cannot equip this item."}"); return;
                }
                boolean nowEquipped = !ii.isEquipped();
                if (nowEquipped) inventoryDAO.equip(userId, itemId, ii.getItemType());
                else inventoryDAO.unequip(userId, itemId);
                recalcStats(userId);
                result = new LinkedHashMap<>();
                result.put("success", true);
                result.put("message", nowEquipped
                    ? "Equipped " + ii.getName() + "."
                    : "Unequipped " + ii.getName() + ".");
                result.put("equipped", nowEquipped);
            } else {
                resp.getWriter().write("{"success":false,"message":"Unknown action."}"); return;
            }
            resp.getWriter().write(GameServlet.toJson(result));
        } catch (NumberFormatException e) {
            resp.getWriter().write("{"success":false,"message":"Invalid item ID."}");
        } catch (Exception e) {
            resp.getWriter().write("{"success":false,"message":"" + esc(e.getMessage()) + ""}");
        }
    }

    private void recalcStats(int userId) throws Exception {
        List<InventoryItem> inv = inventoryDAO.findByUser(userId);
        PlayerStats base = userDAO.loadStats(userId);
        int atk = 10 + (base.getLevel()-1)*3;
        int def = 5  + (base.getLevel()-1)*2;
        for (InventoryItem ii : inv) {
            if (ii.isEquipped()) { atk += ii.getAttackBonus(); def += ii.getDefenseBonus(); }
        }
        base.setAttack(atk); base.setDefense(def);
        userDAO.saveStats(base);
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\"");
    }
}
