package ru.msu.cmc.webprak.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprak.dao.CoverDao;
import ru.msu.cmc.webprak.dao.EditionDao;
import ru.msu.cmc.webprak.dao.OrderDao;
import ru.msu.cmc.webprak.dao.OrderItemDao;
import ru.msu.cmc.webprak.dao.UserDao;
import ru.msu.cmc.webprak.dao.WorkDao;
import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.entity.Work;
import ru.msu.cmc.webprak.enums.ItemStatus;
import ru.msu.cmc.webprak.enums.OrderStatus;
import ru.msu.cmc.webprak.enums.UserRole;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {

    private static final int PAGE_SIZE = 100;

    private final EditionDao editionDao;
    private final WorkDao workDao;
    private final CoverDao coverDao;
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final UserDao userDao;

    public AdminController(EditionDao editionDao, WorkDao workDao, CoverDao coverDao, OrderDao orderDao, OrderItemDao orderItemDao, UserDao userDao) {
        this.editionDao = editionDao;
        this.workDao = workDao;
        this.coverDao = coverDao;
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.userDao = userDao;
    }

    @GetMapping("/admin")
    public String adminIndex(Model model, HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Панель управления");
        return "admin/index";
    }

    @GetMapping("/admin/books")
    public String adminBooks(@RequestParam(required = false) String title,
                             @RequestParam(required = false) String author,
                             @RequestParam(required = false) Boolean available,
                             Model model,
                             HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        model.addAttribute("books", editionDao.searchCatalog(
                title,
                author,
                null,
                null,
                null,
                null,
                available,
                0,
                PAGE_SIZE));
        return "admin/books";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(@RequestParam(required = false) OrderStatus status,
                              @RequestParam(required = false) String client,
                              Model model,
                              HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderDao.searchOrders(status, findClient(client), null, null, 0, PAGE_SIZE));
        return "admin/orders";
    }

    @PostMapping("/admin/books")
    public String addBook(@RequestParam String title,
                          @RequestParam String publisher,
                          @RequestParam String language,
                          @RequestParam BigDecimal price,
                          @RequestParam Integer quantity,
                          Model model,
                          HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        Work work = new Work();
        work.setTitle(title);
        work.setDescription("");
        work.setAuthors(new ArrayList<>());
        workDao.save(work);

        Cover cover = coverDao.findByCoverName("Твердая обложка").orElseGet(() -> {
            Cover newCover = new Cover();
            newCover.setCoverName("Твердая обложка");
            return coverDao.save(newCover);
        });

        Edition edition = new Edition();
        edition.setWork(work);
        edition.setCover(cover);
        edition.setPublisher(publisher);
        edition.setLanguage(language);
        edition.setPrice(price);
        edition.setQuantity(quantity);
        editionDao.save(edition);

        return "redirect:/admin/books";
    }

    @PostMapping("/admin/books/{id}/update")
    public String updateBook(@PathVariable Integer id,
                             @RequestParam String publisher,
                             @RequestParam String language,
                             @RequestParam BigDecimal price,
                             @RequestParam Integer quantity,
                             Model model,
                             HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        editionDao.getById(id).ifPresent(edition -> {
            edition.setPublisher(publisher);
            edition.setLanguage(language);
            edition.setPrice(price);
            edition.setQuantity(quantity);
            editionDao.update(edition);
        });
        return "redirect:/admin/books";
    }

    @PostMapping("/admin/books/{id}/delete")
    public String deleteBook(@PathVariable Integer id, Model model, HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        editionDao.deleteById(id);
        return "redirect:/admin/books";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Integer id,
                                    @RequestParam OrderStatus status,
                                    Model model,
                                    HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        orderDao.updateStatus(id, status);
        if (status == OrderStatus.DELIVERED) {
            var itemIds = orderItemDao.findByOrderId(id).stream()
                    .map(item -> item.getId())
                    .toList();
            orderItemDao.updateStatusByIds(itemIds, ItemStatus.DELIVERED);
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/admin/orders/{id}/refund")
    public String approveRefund(@PathVariable Integer id,
                                @RequestParam(required = false) List<Integer> itemIds,
                                Model model,
                                HttpSession session) {
        if (!isEmployee(session)) {
            return "redirect:/login";
        }

        if (itemIds == null || itemIds.isEmpty()) {
            return "redirect:/orders/" + id;
        }

        orderItemDao.updateStatusByIds(itemIds, ItemStatus.RETURNED);
        long deliveredItems = orderItemDao.findByOrderId(id).stream()
                .filter(item -> item.getStatus() == ItemStatus.DELIVERED)
                .count();
        orderDao.updateStatus(id, deliveredItems == 0 ? OrderStatus.REFUND_ACCEPTED : OrderStatus.PARTIAL_REFUND);
        return "redirect:/orders/" + id;
    }

    private User findClient(String client) {
        if (client == null || client.isBlank()) {
            return null;
        }
        try {
            return userDao.getById(Integer.parseInt(client)).orElse(null);
        } catch (NumberFormatException ignored) {
            return userDao.findByLogin(client)
                    .or(() -> userDao.searchByName(client, 0, 1).stream().findFirst())
                    .orElse(null);
        }
    }

    private boolean isEmployee(HttpSession session) {
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
