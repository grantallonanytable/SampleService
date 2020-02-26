package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Событие для поддержки возможности записи данных в Cassandra.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public interface OrderEntityEvent extends Jsonable, AggregateEvent<OrderEntityEvent> {

    int NUM_SHARDS = 50;

    AggregateEventShards<OrderEntityEvent> TAG = AggregateEventTag.sharded(OrderEntityEvent.class, NUM_SHARDS);

    /**
     * Получить заказ, чьи события записываются.
     * @return заказ
     */
    Order getOrder();

    /**
     * Теги для событий. Состоят из имени класса и номера шарда.
     * @return {@code AggregateEventShards<OrderEntityEvent>}
     */
    @Override
    default AggregateEventShards<OrderEntityEvent> aggregateTag() {
        return TAG;
    }

    /**
     * Событие добавления заказа в Cassandra.
     *
     * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
     */
    @Value
    @AllArgsConstructor(onConstructor = @__({@JsonCreator}))
    final class OrderEntityInserted implements OrderEntityEvent {
        Order order;

    }

}
