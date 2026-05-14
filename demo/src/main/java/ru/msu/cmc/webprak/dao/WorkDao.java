package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import java.util.List;
import java.util.Optional;

public interface WorkDao extends BaseDao<Work, Integer> {

    List<Work> findByTitle(String title);

    List<Work> searchByTitleIgnoreCase(String titlePart, int offset, int limit);

    List<Work> findByAuthor(Author author);

    List<Work> findByAuthor(Author author, int offset, int limit);

    List<Work> searchByAuthorNameIgnoreCase(String authorNamePart, int offset, int limit);

    Optional<Work> findByIdWithAuthors(Integer workId);

    long countByAuthor(Author author);
}
