package com.cy.gulimall.order;

import org.junit.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GulimallOrderApplicationTests {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    public void contextLoads() {
    }

}
