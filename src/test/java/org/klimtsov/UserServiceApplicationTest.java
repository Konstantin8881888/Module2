package org.klimtsov;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UserServiceApplicationTest {

    @Test
    void contextLoads(ApplicationContext context) {
        //Проверяем, что контекст Spring загружается.
        assertNotNull(context);
    }
}