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
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            //persist назначит id после flush/commit.
            session.persist(user);
            session.flush();
            transaction.commit();
            logger.debug("Created user: {}", user);
            return user.getId();
        } catch (SQLGrammarException sqlGr) {
            safeRollback(transaction);
            logger.error("SQL grammar error on update", sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            safeRollback(transaction);
            logger.error("DB connection / persistence problem", connEx);
            throw new DaoException("Не удалось подключиться к базе данных. Проверьте, что PostgreSQL запущен и параметры подключения верны.", connEx);
        } catch (Exception e) {
            safeRollback(transaction);
            logger.error("Error creating user", e);
            throw new DaoException("Ошибка при создании пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            logger.debug("findById({}) -> {}", id, user);
            return Optional.ofNullable(user);
        } catch (SQLGrammarException sqlGr) {
            logger.error("SQL grammar error on update", sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            logger.error("DB connection / persistence problem on findById", connEx);
            throw new DaoException("Не удалось подключиться к базе данных при попытке найти пользователя.", connEx);
        } catch (Exception e) {
            logger.error("Error finding user by id={}", id, e);
            throw new DaoException("Ошибка при поиске пользователя", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            //Сортируем по id в порядке возрастания.
            List<User> list = session.createQuery("from org.klimtsov.userservice.model.User u order by u.id", User.class).list();
            logger.debug("findAll() size={}", list.size());
            return list;
        } catch (SQLGrammarException sqlGr) {
            logger.error("SQL grammar error on update", sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            logger.error("DB connection / persistence problem on findAll", connEx);
            throw new DaoException("Не удалось подключиться к базе данных при попытке получить список пользователей.", connEx);
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new DaoException("Ошибка при получении списка пользователей", e);
        }
    }


    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            logger.debug("Updated user: {}", user);
        } catch (SQLGrammarException sqlGr) {
            safeRollback(transaction);
            logger.error("SQL grammar error on update", sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            safeRollback(transaction);
            logger.error("DB connection / persistence problem on update", connEx);
            throw new DaoException("Не удалось подключиться к базе данных при обновлении пользователя.", connEx);
        } catch (Exception e) {
            safeRollback(transaction);
            logger.error("Error updating user", e);
            throw new DaoException("Ошибка при обновлении пользователя", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User u = session.get(User.class, id);
            if (u != null) {
                session.remove(u);
                logger.debug("Deleted user: {}", u);
                transaction.commit();
                return true;
            } else {
                logger.debug("No user to delete with id={}", id);
                transaction.commit(); //Просто закомитим пустую транзакцию чтобы завершилась.
                return false;
            }
        } catch (SQLGrammarException sqlGr) {
            safeRollback(transaction);
            logger.error("SQL grammar error on update", sqlGr);
            throw new DaoException("Внутренняя ошибка запроса к БД", sqlGr);
        } catch (PersistenceException connEx) {
            safeRollback(transaction);
            logger.error("DB connection / persistence problem on delete", connEx);
            throw new DaoException("Не удалось подключиться к базе данных при удалении пользователя.", connEx);
        } catch (Exception e) {
            safeRollback(transaction);
            logger.error("Error deleting user id={}", id, e);
            throw new DaoException("Ошибка при удалении пользователя", e);
        }
    }

    //Безопасная попытка отката.
    private void safeRollback(Transaction transaction) {
        if (transaction != null) {
            try {
                transaction.rollback();
            } catch (Exception ex) {
                logger.warn("Rollback failed", ex);
            }
        }
    }
}