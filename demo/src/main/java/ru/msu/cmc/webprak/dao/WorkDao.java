package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;

public interface WorkDao extends BaseDao<Work, Integer> {

    List<Work> findByTitle(String title);

    List<Work> findByAuthor(Author author);
}
