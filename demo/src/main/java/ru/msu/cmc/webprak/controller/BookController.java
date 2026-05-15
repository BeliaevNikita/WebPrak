package ru.msu.cmc.webprak.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.msu.cmc.webprak.dao.EditionDao;

@Controller
public class BookController {

    private final EditionDao editionDao;

    public BookController(EditionDao editionDao) {
        this.editionDao = editionDao;
    }

    @GetMapping("/book/{id}")
    public String book(@PathVariable Integer id, Model model) {
        return editionDao.findByIdWithDetails(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "book";
                })
                .orElse("error");
    }
}
