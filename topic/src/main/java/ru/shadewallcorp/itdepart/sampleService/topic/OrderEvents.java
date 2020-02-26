package ru.shadewallcorp.itdepart.sampleService.topic;

import com.lightbend.lagom.javadsl.api.broker.Topic;

/**
 * Order topic service.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public interface OrderEvents {

    String TOPIC_ORDER = "order-events-topic";

    /**
     * Топик заказов и лидов (различаются по типу объекта).
     *
     * @return a Kafka {@link Topic}
     */
    Topic<OrderEvent> getTopicOrder();

}
