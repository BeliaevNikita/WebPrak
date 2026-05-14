package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Order;
import ru.msu.cmc.webprak.entity.OrderItem;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.entity.Work;
import ru.msu.cmc.webprak.enums.ItemStatus;
import ru.msu.cmc.webprak.enums.OrderStatus;
import ru.msu.cmc.webprak.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class DaoTestData {

    private DaoTestData() {
    }

    static Author author(String firstName, String lastName) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setWorks(new ArrayList<>());
        return author;
    }

    static Cover cover(String coverName) {
        Cover cover = new Cover();
        cover.setCoverName(coverName);
        return cover;
    }

    static Work work(String title, Author... authors) {
        Work work = new Work();
        work.setTitle(title);
        work.setDescription(title + " description");
        work.setAuthors(new ArrayList<>(List.of(authors)));
        return work;
    }

    static Edition edition(Work work, Cover cover, int quantity, BigDecimal price) {
        Edition edition = new Edition();
        edition.setWork(work);
        edition.setCover(cover);
        edition.setPageCount(320);
        edition.setPublicationDate(LocalDate.of(2024, 1, 1));
        edition.setPublisher("Test Publisher");
        edition.setLanguage("RU");
        edition.setQuantity(quantity);
        edition.setPrice(price);
        return edition;
    }

    static User user(String login, UserRole role) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("password");
        user.setRole(role);
        user.setContacts("{\"email\":\"" + login + "@example.com\"}");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setGender(Boolean.TRUE);
        user.setFirstName("First");
        user.setLastName("Last");
        return user;
    }

    static Order order(User client, OrderStatus status) {
        Order order = new Order();
        order.setClient(client);
        order.setOrderDate(LocalDate.of(2024, 2, 1));
        order.setStatus(status);
        order.setDeliveryAddress("Test address");
        order.setItems(new ArrayList<>());
        return order;
    }

    static OrderItem orderItem(Order order, Edition edition, int quantity, ItemStatus status) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setEdition(edition);
        item.setQuantity(quantity);
        item.setStatus(status);
        return item;
    }
}
