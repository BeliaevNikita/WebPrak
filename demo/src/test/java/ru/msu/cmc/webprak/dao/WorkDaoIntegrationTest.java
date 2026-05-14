package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Work;

import static org.assertj.core.api.Assertions.assertThat;

class WorkDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveFindUpdateDelete() {
        Author author = authorDao.save(DaoTestData.author("Mikhail", "Bulgakov"));
        Work work = workDao.save(DaoTestData.work("Master and Margarita", author));

        assertThat(workDao.getById(work.getId())).isPresent();

        work.setDescription("Updated");
        workDao.update(work);

        assertThat(workDao.getById(work.getId()).orElseThrow().getDescription()).isEqualTo("Updated");

        workDao.deleteById(work.getId());
        assertThat(workDao.getById(work.getId())).isEmpty();
    }

    @Test
    void findByTitleIsExactAndCaseSensitive() {
        workDao.save(DaoTestData.work("Hamlet"));

        assertThat(workDao.findByTitle("Hamlet")).hasSize(1);
        assertThat(workDao.findByTitle("hamlet")).isEmpty();
    }

    @Test
    void findByAuthorReturnsWorksForAuthor() {
        Author author = authorDao.save(DaoTestData.author("Alexander", "Pushkin"));
        workDao.save(DaoTestData.work("Eugene Onegin", author));
        workDao.save(DaoTestData.work("The Captain's Daughter", author));

        assertThat(workDao.findByAuthor(author)).hasSize(2);
    }

    @Test
    void searchPaginationCountAndFetchAuthorsSupportCatalogUseCases() {
        Author author = authorDao.save(DaoTestData.author("Jane", "Austen"));
        Work work = workDao.save(DaoTestData.work("Pride and Prejudice", author));

        assertThat(workDao.searchByTitleIgnoreCase("pride", 0, 10)).contains(work);
        assertThat(workDao.findByAuthor(author, 0, 10)).contains(work);
        assertThat(workDao.searchByAuthorNameIgnoreCase("aust", 0, 10)).contains(work);
        assertThat(workDao.findByIdWithAuthors(work.getId())).isPresent();
        assertThat(workDao.countByAuthor(author)).isEqualTo(1);
    }
}
