package ru.msu.cmc.webprak.dao;

import ru.msu.cmc.webprak.entity.User;
import ru.msu.cmc.webprak.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserDao extends BaseDao<User, Integer> {

    Optional<User> findByLogin(String login);

    List<User> findByRole(UserRole role);
}
