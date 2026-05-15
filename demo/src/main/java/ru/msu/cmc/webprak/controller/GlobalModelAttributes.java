package ru.msu.cmc.webprak.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.UserRole;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserDao userDao;

    public GlobalModelAttributes(UserDao userDao) {
        this.userDao = userDao;
    }

    @ModelAttribute("isEmployee")
    public boolean isEmployee(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer id) {
            return userDao.getById(id)
                    .map(User::getRole)
                    .filter(UserRole.EMPLOYEE::equals)
                    .isPresent();
        }
        return false;
    }
}
