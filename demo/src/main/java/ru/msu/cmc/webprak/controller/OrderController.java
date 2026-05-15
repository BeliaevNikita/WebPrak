package ru.msu.cmc.webprak.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprak.dao.EditionDao;
import ru.msu.cmc.webprak.dao.OrderDao;
import ru.msu.cmc.webprak.dao.OrderItemDao;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.OrderStatus;
import ru.msu.cmc.webprak.enums.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;

@Controller
public class OrderController {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final EditionDao editionDao;
    private final UserDao userDao;

    public OrderController(OrderDao orderDao, OrderItemDao orderItemDao, EditionDao editionDao, UserDao userDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.editionDao = editionDao;
        this.userDao = userDao;
    }

    @GetMapping("/orders/{id}")
    public String order(@PathVariable Integer id, Model model, HttpSession session) {
        return orderDao.getById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("items", orderItemDao.findByOrderId(id));
                    model.addAttribute("total", orderDao.calculateTotal(id));
                    model.addAttribute("isEmployee", currentUser(session).getRole() == UserRole.EMPLOYEE);
                    return "order";
                })
                .orElse("error");
    }

    @PostMapping("/orders/{id}/checkout")
    public String checkout(@PathVariable Integer id,
                           @RequestParam String deliveryAddress,
                           Model model) {
        if (deliveryAddress.isBlank()) {
            return "redirect:/orders/" + id;
        }

        orderDao.checkout(id, deliveryAddress.trim(), LocalDate.now());
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/refund")
    public String refund(@PathVariable Integer id, Model model) {
        orderDao.updateStatus(id, OrderStatus.REFUND_ATTEMPT);
        return "redirect:/orders/" + id;
    }

    @PostMapping("/cart/items")
    public String addCartItem(@RequestParam Integer editionId,
                              @RequestParam Integer quantity,
                              Model model,
                              HttpSession session) {
        User user = currentUser(session);
        if (user.getId() == null) {
            return "redirect:/login";
        }

        Edition edition = editionDao.getById(editionId).orElse(null);
        if (edition == null) {
            return "redirect:/catalog";
        }

        Order cart = orderDao.findActiveCartByClient(user).orElseGet(() -> {
            Order newCart = new Order();
            newCart.setClient(user);
            newCart.setOrderDate(LocalDate.now());
            newCart.setStatus(OrderStatus.DRAFT);
            newCart.setDeliveryAddress("");
            newCart.setItems(new ArrayList<>());
            return orderDao.save(newCart);
        });

        orderItemDao.addToCart(cart, edition, quantity);
        return "redirect:/orders/" + cart.getId();
    }

    private User currentUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer id) {
            return userDao.getById(id).orElseGet(User::new);
        }
        return new User();
    }
}
