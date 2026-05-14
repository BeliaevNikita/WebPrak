package ru.msu.cmc.webprak.dao;

import org.junit.jupiter.api.Test;
import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Test
    void saveFindUpdateDelete() {
        User user = userDao.save(DaoTestData.user("customer1", UserRole.CUSTOMER));

        assertThat(userDao.getById(user.getId())).isPresent();

        user.setLastName("Updated");
        userDao.update(user);

        assertThat(userDao.getById(user.getId()).orElseThrow().getLastName()).isEqualTo("Updated");

        userDao.deleteById(user.getId());
        assertThat(userDao.getById(user.getId())).isEmpty();
    }

    @Test
    void findByLoginReturnsOptional() {
        userDao.save(DaoTestData.user("uniqueLogin", UserRole.CUSTOMER));

        assertThat(userDao.findByLogin("uniqueLogin")).isPresent();
        assertThat(userDao.findByLogin("missing")).isEmpty();
    }

    @Test
    void findByRoleReturnsAllUsersWithRole() {
        userDao.save(DaoTestData.user("customer2", UserRole.CUSTOMER));
        userDao.save(DaoTestData.user("employee1", UserRole.EMPLOYEE));

        assertThat(userDao.findByRole(UserRole.CUSTOMER)).hasSize(1);
        assertThat(userDao.findByRole(UserRole.EMPLOYEE)).hasSize(1);
    }

    @Test
    void loginEmailExistsAndAuthenticationMethodsSupportUserUseCases() {
        User user = userDao.save(DaoTestData.user("authUser", UserRole.CUSTOMER));

        assertThat(userDao.findByEmail("authUser@example.com")).isPresent();
        assertThat(userDao.findByLoginAndPassword("authUser", "password")).contains(user);
        assertThat(userDao.existsByLogin("authUser")).isTrue();
        assertThat(userDao.existsByEmail("authUser@example.com")).isTrue();
        assertThat(userDao.searchByName("auth", 0, 10)).contains(user);
    }
}
