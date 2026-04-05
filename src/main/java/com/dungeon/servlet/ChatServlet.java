package com.dungeon.servlet;
import com.dungeon.dao.ChatDAO;
import com.dungeon.model.ChatMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name="ChatServlet", urlPatterns={"/chat"})
public class ChatServlet extends HttpServlet {
    private final ChatDAO chatDAO = new ChatDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json;charset=UTF-8");
        if (session == null || session.getAttribute("userId") == null) {
            resp.getWriter().write("[]"); return;
        }
        String channel = req.getParameter("channel"); if (channel == null) channel = "global";
        String sinceStr = req.getParameter("since");
        long since = 0; try { since = Long.parseLong(sinceStr); } catch (Exception ignored) {}
        try {
            List<ChatMessage> msgs = sinceStr == null
                ? chatDAO.getRecent(channel, 40) : chatDAO.pollSince(channel, since);
            resp.getWriter().write(toJson(msgs));
        } catch (Exception e) { resp.getWriter().write("[]"); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json;charset=UTF-8");
        if (session == null || session.getAttribute("userId") == null) {
            resp.getWriter().write("{"success":false}"); return;
        }
        int userId = (int) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String message = req.getParameter("message");
        String channel = req.getParameter("channel"); if (channel == null) channel = "global";
        if (message == null || message.isBlank() || message.length() > 300) {
            resp.getWriter().write("{"success":false,"message":"Invalid message."}"); return;
        }
        try { chatDAO.save(userId, username, message.trim(), channel);
            resp.getWriter().write("{"success":true}");
        } catch (Exception e) { resp.getWriter().write("{"success":false}"); }
    }

    private String toJson(List<ChatMessage> msgs) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < msgs.size(); i++) {
            ChatMessage m = msgs.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"id\":").append(m.getId()).append(",")
              .append("\"username\":\"").append(esc(m.getUsername())).append("\",")
              .append("\"message\":\"").append(esc(m.getMessage())).append("\",")
              .append("\"time\":\"").append(m.getTimeFormatted()).append("\"")
              .append("}");
        }
        return sb.append("]").toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\"")
                .replace("<","&lt;").replace(">","&gt;");
    }
}
