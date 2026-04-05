package com.dungeon.servlet;
import com.dungeon.dao.UserDAO;
import com.dungeon.model.User;
import com.dungeon.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name="AuthServlet", urlPatterns={"/auth"})
public class AuthServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String action = req.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "login"    -> handleLogin(req, resp);
                case "register" -> handleRegister(req, resp);
                case "logout"   -> handleLogout(req, resp);
                default -> resp.getWriter().write("{\"success\":false,\"message\":\"Unknown action\"}");
            }
        } catch (Exception e) {
            resp.getWriter().write("{\"success\":false,\"message\":\"" + esc(e.getMessage()) + "\"}");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Username and password required.\"}");
            return;
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid credentials.\"}");
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("userId",   user.getId());
        session.setAttribute("username", user.getUsername());
        userDAO.setOnline(user.getId(), true);
        resp.getWriter().write("{\"success\":true,\"message\":\"Welcome back, "
            + user.getUsername() + "!\",\"redirect\":\"game\"}");
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email    = req.getParameter("email");
        if (username == null || password == null || email == null
                || username.isBlank() || password.isBlank() || email.isBlank()) {
            resp.getWriter().write("{\"success\":false,\"message\":\"All fields required.\"}");
            return;
        }
        if (username.length() < 3 || username.length() > 20) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Username must be 3-20 characters.\"}");
            return;
        }
        if (password.length() < 6) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Password must be at least 6 characters.\"}");
            return;
        }
        if (userDAO.findByUsername(username.trim()) != null) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Username already taken.\"}");
            return;
        }
        User user = userDAO.register(username.trim(), PasswordUtil.hash(password), email.trim());
        HttpSession session = req.getSession(true);
        session.setAttribute("userId",   user.getId());
        session.setAttribute("username", user.getUsername());
        userDAO.setOnline(user.getId(), true);
        resp.getWriter().write("{\"success\":true,\"message\":\"Account created! Welcome to Dungeon Realm!\",\"redirect\":\"game\"}");
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId != null) userDAO.setOnline(userId, false);
            session.invalidate();
        }
        resp.getWriter().write("{\"success\":true,\"redirect\":\".\"}");
    }

    private String esc(String s) {
        if (s == null) return "Server error";
        return s.replace("\"","'").replace("\n"," ").replace("\r","");
    }
}
