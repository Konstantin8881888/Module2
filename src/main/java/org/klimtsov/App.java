package org.klimtsov;

import org.hibernate.Session;

public class App {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Connected, dialect: " + session.getSessionFactory().getProperties().get("hibernate.dialect"));
        }
    }
}
