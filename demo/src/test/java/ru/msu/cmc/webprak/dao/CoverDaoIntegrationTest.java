package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.Cover;

import static org.assertj.core.api.Assertions.assertThat;

class CoverDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveFindUpdateDelete() {
        Cover cover = coverDao.save(DaoTestData.cover("Hardcover"));

        assertThat(coverDao.getById(cover.getId())).isPresent();

        cover.setCoverName("Paperback");
        coverDao.update(cover);

        assertThat(coverDao.findByCoverName("Paperback")).isPresent();

        coverDao.delete(cover);
        assertThat(coverDao.getById(cover.getId())).isEmpty();
    }

    @Test
    void findByCoverNameReturnsOptional() {
        coverDao.save(DaoTestData.cover("Dust jacket"));

        assertThat(coverDao.findByCoverName("Dust jacket")).isPresent();
        assertThat(coverDao.findByCoverName("Missing")).isEmpty();
    }

    @Test
    void searchExistsAndFindByWorkSupportCatalogFilters() {
        Cover cover = coverDao.save(DaoTestData.cover("Premium Hardcover"));
        var author = authorDao.save(DaoTestData.author("Cover", "Author"));
        var work = workDao.save(DaoTestData.work("Covered Work", author));
        editionDao.save(DaoTestData.edition(work, cover, 1, new java.math.BigDecimal("100.00")));

        assertThat(coverDao.searchByCoverNameIgnoreCase("premium")).contains(cover);
        assertThat(coverDao.existsByCoverName("Premium Hardcover")).isTrue();
        assertThat(coverDao.findByWork(work)).contains(cover);
    }
}
