package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Комманда для поддержки возможности записи данных в Cassandra.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public interface OrderEntityCommand extends Jsonable {
    /**
     * Добавить заказ.
     */
    @Value
    @AllArgsConstructor(onConstructor = @__({@JsonCreator}))
    final class InsertOrder implements OrderEntityCommand, ReplyType<InsertOrderDone> {
        private final Order order;

    }

    /**
     * Результат выполнения команды вставки заказа.
     */
    @Value
    @AllArgsConstructor(onConstructor = @__({@JsonCreator}))
    final class InsertOrderDone implements Jsonable {
        private final String entityId;
        private final Order order;

    }

}
