package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;

public interface AuthorDao extends BaseDao<Author, Integer> {

    List<Author> findByLastName(String lastName);

    List<Author> findByName(String firstName, String lastName);

    List<Author> searchByNamePartIgnoreCase(String namePart, int offset, int limit);

    List<Author> findByWork(Work work);

    boolean existsByName(String firstName, String lastName);
}
