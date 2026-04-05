package com.dungeon.servlet;
import com.dungeon.dao.LeaderboardDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(name="LeaderboardServlet", urlPatterns={"/leaderboard"})
public class LeaderboardServlet extends HttpServlet {
    private final LeaderboardDAO lbDAO = new LeaderboardDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<Map<String,Object>> top = lbDAO.getTop(20);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < top.size(); i++) {
                if (i > 0) sb.append(",");
                Map<String,Object> row = top.get(i);
                sb.append("{")
                  .append("\"rank\":").append(row.get("rank")).append(",")
                  .append("\"username\":\"").append(row.get("username")).append("\",")
                  .append("\"level\":").append(row.get("level")).append(",")
                  .append("\"xp\":").append(row.get("xp")).append(",")
                  .append("\"gold\":").append(row.get("gold")).append(",")
                  .append("\"kills\":").append(row.get("kills")).append(",")
                  .append("\"deaths\":").append(row.get("deaths")).append(",")
                  .append("\"online\":").append(row.get("online"))
                  .append("}");
            }
            resp.getWriter().write(sb.append("]").toString());
        } catch (Exception e) { resp.getWriter().write("[]"); }
    }
}
