package org.klimtsov.dao;

import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.SQLGrammarException;
import org.klimtsov.HibernateUtil;
import org.klimtsov.userservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public Long create(User user) {
        logger.info("Создание пользователя: email={}", user.getEmail());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            logger.info("Начало транзакции для создания пользователя: email={}", user.getEmail());

            session.persist(user);
            session.flush();
            transaction.commit();

            logger.info("Пользователь создан успешно: id={}, email={}", user.getId(), user.getEmail());
            logger.debug("Данные созданного пользователя: {}", user);
            return user.getId();
        } catch (SQLGrammarException sqlGr) {
            safeRollback(transaction);
            logger.error("Ошибка SQL при создании пользователя {}: {}", user.getEmail(), sqlGr.getMessage(), sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            safeRollback(transaction);
            logger.error("Проблема подключения к БД при создании пользователя {}: {}", user.getEmail(), connEx.getMessage(), connEx);
            throw new DaoException("Не удалось подключиться к базе данных. Проверьте, что PostgreSQL запущен и параметры подключения верны.", connEx);
        } catch (Exception e) {
            safeRollback(transaction);
            logger.error("Неизвестная ошибка при создании пользователя {}: {}", user.getEmail(), e.getMessage(), e);
            throw new DaoException("Ошибка при создании пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.info("Поиск пользователя по id: {}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user != null) {
                logger.info("Пользователь найден: id={}, email={}", id, user.getEmail());
                logger.debug("Данные найденного пользователя: {}", user);
            } else {
                logger.info("Пользователь не найден: id={}", id);
            }
            return Optional.ofNullable(user);
        } catch (SQLGrammarException sqlGr) {
            logger.error("Ошибка SQL при поиске пользователя по id {}: {}", id, sqlGr.getMessage(), sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            logger.error("Проблема подключения к БД при поиске пользователя по id {}: {}", id, connEx.getMessage(), connEx);
            throw new DaoException("Не удалось подключиться к базе данных при попытке найти пользователя.", connEx);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при поиске пользователя по id={}: {}", id, e.getMessage(), e);
            throw new DaoException("Ошибка при поиске пользователя", e);
        }
    }

    @Override
    public List<User> findAll() {
        logger.info("Запрос всех пользователей");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> list = session.createQuery("from org.klimtsov.userservice.model.User u order by u.id", User.class).list();
            logger.info("Найдено пользователей: {}", list.size());
            logger.debug("Список всех пользователей: {}", list);
            return list;
        } catch (SQLGrammarException sqlGr) {
            logger.error("Ошибка SQL при получении списка пользователей: {}", sqlGr.getMessage(), sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            logger.error("Проблема подключения к БД при получении списка пользователей: {}", connEx.getMessage(), connEx);
            throw new DaoException("Не удалось подключиться к базе данных при попытке получить список пользователей.", connEx);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при получении списка пользователей: {}", e.getMessage(), e);
            throw new DaoException("Ошибка при получении списка пользователей", e);
        }
    }

    @Override
    public void update(User user) {
        logger.info("Обновление пользователя: id={}, email={}", user.getId(), user.getEmail());
        logger.debug("Новые данные для обновления: {}", user);
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            logger.debug("Начало транзакции для обновления пользователя: id={}", user.getId());

            session.merge(user);
            transaction.commit();

            logger.info("Пользователь обновлен успешно: id={}", user.getId());
            logger.debug("Обновленные данные пользователя: {}", user);
        } catch (SQLGrammarException sqlGr) {
            safeRollback(transaction);
            logger.error("Ошибка SQL при обновлении пользователя {}: {}", user.getId(), sqlGr.getMessage(), sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            safeRollback(transaction);
            logger.error("Проблема подключения к БД при обновлении пользователя {}: {}", user.getId(), connEx.getMessage(), connEx);
            throw new DaoException("Не удалось подключиться к базе данных при обновлении пользователя.", connEx);
        } catch (Exception e) {
            safeRollback(transaction);
            logger.error("Неизвестная ошибка при обновлении пользователя {}: {}", user.getId(), e.getMessage(), e);
            throw new DaoException("Ошибка при обновлении пользователя", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        logger.info("Удаление пользователя: id={}", id);
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            logger.debug("Начало транзакции для удаления пользователя: id={}", id);

            User u = session.get(User.class, id);
            if (u != null) {
                session.remove(u);
                transaction.commit();
                logger.info("Пользователь удален успешно: id={}, email={}", id, u.getEmail());
                logger.debug("Данные удаленного пользователя: {}", u);
                return true;
            } else {
                logger.warn("Попытка удаления несуществующего пользователя: id={}", id);
                transaction.commit();
                return false;
            }
        } catch (SQLGrammarException sqlGr) {
            safeRollback(transaction);
            logger.error("Ошибка SQL при удалении пользователя {}: {}", id, sqlGr.getMessage(), sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            safeRollback(transaction);
            logger.error("Проблема подключения к БД при удалении пользователя {}: {}", id, connEx.getMessage(), connEx);
            throw new DaoException("Не удалось подключиться к базе данных при удалении пользователя.", connEx);
        } catch (Exception e) {
            safeRollback(transaction);
            logger.error("Неизвестная ошибка при удалении пользователя {}: {}", id, e.getMessage(), e);
            throw new DaoException("Ошибка при удалении пользователя", e);
        }
    }

    //Безопасная попытка отката.
    private void safeRollback(Transaction transaction) {
        if (transaction != null) {
            try {
                logger.debug("Попытка отката транзакции");
                transaction.rollback();
                logger.debug("Транзакция успешно откатана");
            } catch (Exception ex) {
                logger.error("Ошибка при откате транзакции: {}", ex.getMessage(), ex);
            }
        }
    }
}