package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.Author;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveAndFindById() {
        Author saved = authorDao.save(DaoTestData.author("Leo", "Tolstoy"));

        assertThat(authorDao.getById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Author::getLastName)
                .isEqualTo("Tolstoy");
    }

    @Test
    void updateChangesAuthorFields() {
        Author author = authorDao.save(DaoTestData.author("Leo", "Tolstoy"));
        author.setFirstName("Lev");

        Author updated = authorDao.update(author);

        assertThat(authorDao.getById(updated.getId()).orElseThrow().getFirstName()).isEqualTo("Lev");
    }

    @Test
    void deleteByIdRemovesAuthor() {
        Author author = authorDao.save(DaoTestData.author("Anton", "Chekhov"));

        authorDao.deleteById(author.getId());

        assertThat(authorDao.getById(author.getId())).isEmpty();
    }

    @Test
    void findByLastNameReturnsAllMatches() {
        authorDao.save(DaoTestData.author("First", "Shared"));
        authorDao.save(DaoTestData.author("Second", "Shared"));

        assertThat(authorDao.findByLastName("Shared")).hasSize(2);
        assertThat(authorDao.findByLastName("Missing")).isEmpty();
    }

    @Test
    void findByNameReturnsExactMatches() {
        authorDao.save(DaoTestData.author("Fyodor", "Dostoevsky"));

        assertThat(authorDao.findByName("Fyodor", "Dostoevsky")).hasSize(1);
        assertThat(authorDao.findByName("fyodor", "Dostoevsky")).isEmpty();
    }

    @Test
    void searchAndExistsMethodsSupportAuthorUseCases() {
        Author author = authorDao.save(DaoTestData.author("Alexander", "Pushkin"));
        workDao.save(DaoTestData.work("Eugene Onegin", author));

        assertThat(authorDao.searchByNamePartIgnoreCase("push", 0, 10)).hasSize(1);
        assertThat(authorDao.existsByName("Alexander", "Pushkin")).isTrue();
        assertThat(authorDao.findByWork(workDao.findByTitle("Eugene Onegin").get(0))).contains(author);
    }
}
