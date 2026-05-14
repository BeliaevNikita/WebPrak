package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.Author;
import ru.msu.cmc.webprak.entity.Cover;
import ru.msu.cmc.webprak.entity.Edition;
import ru.msu.cmc.webprak.entity.Work;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EditionDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveFindUpdateDelete() {
        Edition edition = persistedEdition(5);

        assertThat(editionDao.getById(edition.getId())).isPresent();

        edition.setQuantity(3);
        edition.setPrice(new BigDecimal("499.90"));
        editionDao.update(edition);

        Edition updated = editionDao.getById(edition.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getPrice()).isEqualByComparingTo("499.90");

        editionDao.deleteById(edition.getId());
        assertThat(editionDao.getById(edition.getId())).isEmpty();
    }

    @Test
    void findByWorkReturnsAllEditionsOfBook() {
        Author author = authorDao.save(DaoTestData.author("Leo", "Tolstoy"));
        Work work = workDao.save(DaoTestData.work("War and Peace", author));
        Cover hard = coverDao.save(DaoTestData.cover("Hard"));
        Cover soft = coverDao.save(DaoTestData.cover("Soft"));
        editionDao.save(DaoTestData.edition(work, hard, 1, new BigDecimal("100.00")));
        editionDao.save(DaoTestData.edition(work, soft, 2, new BigDecimal("200.00")));

        assertThat(editionDao.findByWork(work)).hasSize(2);
        assertThat(editionDao.findByWork(work, 0, 1)).hasSize(1);
    }

    @Test
    void findAvailableReturnsOnlyPositiveQuantity() {
        persistedEdition(2);
        persistedEdition(0);

        assertThat(editionDao.findAvailable())
                .extracting(Edition::getQuantity)
                .allMatch(quantity -> quantity > 0);
        assertThat(editionDao.findAvailable(0, 50))
                .extracting(Edition::getQuantity)
                .allMatch(quantity -> quantity > 0);
    }

    @Test
    void catalogSearchCountPopularAndStockMethodsSupportStoreUseCases() {
        Edition edition = persistedEdition(7);

        assertThat(editionDao.findByIdWithDetails(edition.getId())).isPresent();
        assertThat(editionDao.findPopular(50)).contains(edition);
        assertThat(editionDao.searchByTitle("Work 7", 0, 10)).contains(edition);
        assertThat(editionDao.searchByAuthor("Author7", 0, 10)).contains(edition);
        assertThat(editionDao.findByPriceBetween(new BigDecimal("100.00"), new BigDecimal("400.00"), 0, 10))
                .contains(edition);
        assertThat(editionDao.findByPublicationDateBetween(
                java.time.LocalDate.of(2023, 1, 1),
                java.time.LocalDate.of(2025, 1, 1),
                0,
                10)).contains(edition);
        assertThat(editionDao.searchCatalog("Work", "Author", null, null, null, null, true, 0, 10))
                .contains(edition);
        assertThat(editionDao.countCatalog("Work", "Author", null, null, null, null, true)).isPositive();
        assertThat(editionDao.hasStock(edition.getId(), 3)).isTrue();

        assertThat(editionDao.updateStock(edition.getId(), 4)).isEqualTo(1);
        assertThat(editionDao.incrementStock(edition.getId(), 2)).isEqualTo(1);
        assertThat(editionDao.getById(edition.getId()).orElseThrow().getQuantity()).isEqualTo(6);
    }

    private Edition persistedEdition(int quantity) {
        Author author = authorDao.save(DaoTestData.author("Author" + quantity, "Last" + quantity));
        Work work = workDao.save(DaoTestData.work("Work " + quantity, author));
        Cover cover = coverDao.save(DaoTestData.cover("Cover " + quantity));
        return editionDao.save(DaoTestData.edition(work, cover, quantity, new BigDecimal("300.00")));
    }
}
