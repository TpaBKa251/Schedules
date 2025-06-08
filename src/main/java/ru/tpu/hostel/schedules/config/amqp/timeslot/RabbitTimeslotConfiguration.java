package ru.tpu.hostel.schedules.config.amqp.timeslot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
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
import ru.tpu.hostel.internal.config.amqp.AmqpMessagingConfig;
import ru.tpu.hostel.internal.config.amqp.interceptor.AmqpMessageReceiveInterceptor;
import ru.tpu.hostel.internal.external.amqp.Microservice;
import ru.tpu.hostel.schedules.external.amqp.timeslot.TimeslotMessageType;

import java.util.Set;

/**
 * Конфигурация брокера сообщений RabbitMQ для общения с микросервисом броней
 */
@SuppressWarnings("NullableProblems")
@Configuration
@EnableConfigurationProperties({
        RabbitTimeslotProperties.class,
        RabbitBookTimeslotQueueingProperties.class,
        RabbitCancelTimeslotQueueingProperties.class,
        RabbitScheduleServiceTimeslotQueueingProperties.class
})
public class RabbitTimeslotConfiguration {

    public static final String TIMESLOT_LISTENER = "timeslotQueueListener";

    private static final String TIMESLOT_CONNECTION_FACTORY = "timeslotQueueConnectionFactory";

    private static final String TIMESLOT_AMQP_ADMIN = "timeslotQueueAmqpAdmin";

    private static final String TIMESLOT_RABBIT_TEMPLATE = "timeslotQueueRabbitTemplate";

    private static final String TIMESLOT_MESSAGE_CONVERTER = "timeslotQueueMessageConverter";

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
        connectionFactory.setUsername(properties.username());
        connectionFactory.setPassword(properties.password());
        connectionFactory.setVirtualHost(properties.virtualHost());
        connectionFactory.setAddresses(properties.addresses());
        connectionFactory.setConnectionTimeout((int) properties.connectionTimeout().toMillis());
        return connectionFactory;
    }

    @Bean(TIMESLOT_RABBIT_TEMPLATE)
    public RabbitTemplate timeslotQueueRabbitTemplate(
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            @Qualifier(TIMESLOT_MESSAGE_CONVERTER) MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setObservationEnabled(true);
        return rabbitTemplate;
    }

    @Bean(TIMESLOT_AMQP_ADMIN)
    public AmqpAdmin timeslotQueueAmqpAdmin(
            @Qualifier(TIMESLOT_RABBIT_TEMPLATE) RabbitTemplate rabbitTemplate,
            RabbitBookTimeslotQueueingProperties timeslotQueueingProperties,
            RabbitCancelTimeslotQueueingProperties cancelBookingProperties
    ) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitTemplate);
        initQueue(
                rabbitAdmin,
                timeslotQueueingProperties.exchangeName(),
                timeslotQueueingProperties.routingKey(),
                timeslotQueueingProperties.queueName()
        );
        initQueue(
                rabbitAdmin,
                cancelBookingProperties.exchangeName(),
                cancelBookingProperties.routingKey(),
                cancelBookingProperties.queueName()
        );
        return rabbitAdmin;
    }

    private void initQueue(RabbitAdmin rabbitAdmin, String exchangeName, String routingKey, String queueName) {
        DirectExchange exchange = new DirectExchange(exchangeName);

        Queue queue = QueueBuilder.durable(queueName)
                .quorum()
                .build();

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        declareAndBindQueue(rabbitAdmin, routingKey, exchange, queue);
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
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            Tracer tracer,
            OpenTelemetry openTelemetry
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(new AmqpMessageReceiveInterceptor(tracer, openTelemetry));
        return factory;
    }

    @Bean
    public AmqpMessagingConfig bookReplyAmqpMessagingConfig(
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            @Qualifier(TIMESLOT_MESSAGE_CONVERTER) MessageConverter messageConverter,
            RabbitBookTimeslotQueueingProperties properties
    ) {
        return new AmqpMessagingConfig() {
            @Override
            public RabbitTemplate rabbitTemplate() {
                RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
                rabbitTemplate.setMessageConverter(messageConverter);
                rabbitTemplate.setExchange(properties.exchangeName());
                rabbitTemplate.setRoutingKey(properties.routingKey());
                rabbitTemplate.setChannelTransacted(true);
                rabbitTemplate.setObservationEnabled(true);
                return rabbitTemplate;
            }

            @Override
            public MessageProperties messageProperties() {
                return MessagePropertiesBuilder.newInstance()
                        .setPriority(10)
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();
            }

            @Override
            public Set<Microservice> receivingMicroservices() {
                return Set.of(Microservice.BOOKING);
            }

            @Override
            public boolean isApplicable(Enum<?> amqpMessageType) {
                return amqpMessageType == TimeslotMessageType.BOOK_REPLY;
            }
        };
    }

    @Bean
    public AmqpMessagingConfig timslotsAmqpMessagingConfig(
            @Qualifier(TIMESLOT_CONNECTION_FACTORY) ConnectionFactory connectionFactory,
            @Qualifier(TIMESLOT_MESSAGE_CONVERTER) MessageConverter messageConverter,
            RabbitScheduleServiceTimeslotQueueingProperties properties
    ) {
        return new AmqpMessagingConfig() {
            @Override
            public RabbitTemplate rabbitTemplate() {
                RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
                rabbitTemplate.setMessageConverter(messageConverter);
                rabbitTemplate.setExchange(properties.exchangeName());
                rabbitTemplate.setRoutingKey(properties.routingKey());
                rabbitTemplate.setChannelTransacted(true);
                rabbitTemplate.setObservationEnabled(true);
                return rabbitTemplate;
            }

            @Override
            public MessageProperties messageProperties() {
                return MessagePropertiesBuilder.newInstance()
                        .setPriority(10)
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();
            }

            @Override
            public Set<Microservice> receivingMicroservices() {
                return Set.of(Microservice.BOOKING);
            }

            @Override
            public boolean isApplicable(Enum<?> amqpMessageType) {
                return amqpMessageType == TimeslotMessageType.TIMESLOTS;
            }
        };
    }

}
