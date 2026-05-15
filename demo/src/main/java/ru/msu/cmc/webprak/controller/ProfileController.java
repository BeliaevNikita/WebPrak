package ru.msu.cmc.webprak.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprak.dao.OrderDao;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.entity.User;

import java.util.List;

@Controller
public class ProfileController {

    private final UserDao userDao;
    private final OrderDao orderDao;

    public ProfileController(UserDao userDao, OrderDao orderDao) {
        this.userDao = userDao;
        this.orderDao = orderDao;
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User user = currentUser(session);
        if (user.getId() == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("email", extractContact(user.getContacts(), "email"));
        model.addAttribute("phone", extractContact(user.getContacts(), "phone"));
        model.addAttribute("orders", user.getId() == null ? List.of() : orderDao.findByClient(user));
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                Model model,
                                HttpSession session) {
        User user = currentUser(session);
        if (user.getId() != null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setContacts("{\"email\":\"" + email + "\",\"phone\":\"" + nullToEmpty(phone) + "\"}");
            userDao.update(user);
        }
        return "redirect:/profile";
    }

    private User currentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer id) {
            return userDao.getById(id).orElseGet(User::new);
        }
        return new User();
    }

    private String extractContact(String contacts, String key) {
        if (contacts == null) {
            return "";
        }
        String marker = "\"" + key + "\":\"";
        int start = contacts.indexOf(marker);
        if (start < 0) {
            return "";
        }
        int valueStart = start + marker.length();
        int valueEnd = contacts.indexOf('"', valueStart);
        return valueEnd < 0 ? "" : contacts.substring(valueStart, valueEnd);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
