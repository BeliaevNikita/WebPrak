package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Author;

import java.util.List;

public interface AuthorDao extends BaseDao<Author, Integer> {

    List<Author> findByLastName(String lastName);

    List<Author> findByName(String firstName, String lastName);
}
