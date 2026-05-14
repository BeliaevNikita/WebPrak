package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserDao extends BaseDao<User, Integer> {

    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    Optional<User> findByLoginAndPassword(String login, String password);

    List<User> findByRole(UserRole role);

    List<User> searchByName(String namePart, int offset, int limit);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}
