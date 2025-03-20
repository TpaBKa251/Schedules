package ru.tpu.hostel.schedules.config.amqp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tpu.hostel.schedules.rabbit.amqp.AmqpMessageSender;
import ru.tpu.hostel.schedules.rabbit.amqp.timeslot.RabbitTimeslotSender;

/**
 * Конфигурация брокера сообщений RabbitMQ для общения с микросервисом броней
 */
@Configuration
@EnableConfigurationProperties({RabbitTimeslotProperties.class, RabbitTimeslotQueueingProperties.class})
public class RabbitTimeslotConfiguration {

    public static final String TIMESLOT_LISTENER = "timeslotQueueListener";

    private static final String TIMESLOT_CONNECTION_FACTORY = "timeslotQueueConnectionFactory";

    private static final String TIMESLOT_AMQP_ADMIN = "timeslotQueueAmqpAdmin";

    private static final String TIMESLOT_RABBIT_TEMPLATE = "timeslotQueueRabbitTemplate";

    private static final String TIMESLOT_MESSAGE_CONVERTER = "timeslotQueueMessageConverter";

    private static final String TIMESLOT_AMQP_MESSAGE_SENDER = "timeslotAmqpMessageSender";

    @Bean(TIMESLOT_MESSAGE_CONVERTER)
    public MessageConverter timeslotQueueMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean(TIMESLOT_CONNECTION_FACTORY)
    public ConnectionFactory timeslotQueueConnectionFactory(RabbitTimeslotProperties properties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(properties.getUsername());
        connectionFactory.setPassword(properties.getPassword());
        connectionFactory.setVirtualHost(properties.getVirtualHost());
        connectionFactory.setAddresses(properties.getAddresses());
        connectionFactory.setConnectionTimeout((int) properties.getConnectionTimeout().toMillis());
        return connectionFactory;
    }

    @Bean(TIMESLOT_RABBIT_TEMPLATE)
    public RabbitTemplate timeslotQueueRabbitTemplate(
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            @Qualifier(TIMESLOT_MESSAGE_CONVERTER) MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean(TIMESLOT_AMQP_ADMIN)
    public AmqpAdmin timeslotQueueAmqpAdmin(
            @Qualifier(TIMESLOT_RABBIT_TEMPLATE) RabbitTemplate rabbitTemplate,
            RabbitTimeslotQueueingProperties queueProperties
    ) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);
        initQueue(rabbitAdmin, queueProperties);
        return rabbitAdmin;
    }

    private void initQueue(RabbitAdmin rabbitAdmin, RabbitTimeslotQueueingProperties queueProperties) {
        DirectExchange exchange = new DirectExchange(queueProperties.getExchangeName());

        Queue queue = QueueBuilder.durable(queueProperties.getQueueName())
                .quorum()
                .build();

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        declareAndBindQueue(rabbitAdmin, queueProperties.getRoutingKey(), exchange, queue);
    }

    private void declareAndBindQueue(
            RabbitAdmin rabbitAdmin,
            String replyRoutingKey,
            DirectExchange exchange,
            Queue queue
    ) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(replyRoutingKey);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
    }

    @Bean(TIMESLOT_LISTENER)
    public SimpleRabbitListenerContainerFactory timeslotQueueListener(
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Bean(TIMESLOT_AMQP_MESSAGE_SENDER)
    public AmqpMessageSender timeslotAmqpMessageSender(
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            RabbitTimeslotQueueingProperties queueProperties
    ) {
        return new RabbitTimeslotSender(connectionFactory, queueProperties);
    }

}
