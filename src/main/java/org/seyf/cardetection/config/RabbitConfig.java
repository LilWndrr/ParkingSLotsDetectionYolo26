package org.seyf.cardetection.config;


import lombok.Setter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${queue.name}")
    private String queueName;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public Queue queue(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        Queue queue = new Queue(queueName, false);

        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(queue);

        return queue ;
    }
}
