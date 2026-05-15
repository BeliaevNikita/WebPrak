package ru.msu.cmc.webprak.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprak.dao.EditionDao;

import java.math.BigDecimal;

@Controller
public class CatalogController {

    private static final int PAGE_SIZE = 50;

    private final EditionDao editionDao;

    public CatalogController(EditionDao editionDao) {
        this.editionDao = editionDao;
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) String title,
                          @RequestParam(required = false) String author,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {
        int offset = Math.max(page, 0) * PAGE_SIZE;
        model.addAttribute("books", editionDao.searchCatalog(
                title,
                author,
                minPrice,
                maxPrice,
                null,
                null,
                true,
                offset,
                PAGE_SIZE));
        model.addAttribute("nextPage", page + 1);
        return "catalog";
    }
}
