package org.klimtsov;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HibernateUtilTest {

    @Test
    public void sessionFactoryCreation_Success() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        assertNotNull(sessionFactory);
        assertFalse(sessionFactory.isClosed());
    }

    @Test
    public void shutdown_ClosesSessionFactory() {
        HibernateUtil.shutdown();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        assertTrue(sessionFactory.isClosed());
    }
}