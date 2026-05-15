package ru.msu.cmc.webprak.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.enums.UserRole;

@Controller
public class AuthController {

    private final UserDao userDao;

    public AuthController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "Вход");
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String login, @RequestParam String password, Model model, HttpSession session) {
        var user = userDao.findByLoginAndPassword(login, password);
        if (user.isPresent()) {
            session.setAttribute("userId", user.get().getId());
            if (user.get().getRole() == UserRole.EMPLOYEE) {
                return "redirect:/admin";
            }
            return "redirect:/profile";
        }
        model.addAttribute("error", "Неверный логин или пароль");
        return "login";
    }
}
