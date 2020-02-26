package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Состояние объекта для поддержки возможности записи данных в Cassandra.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class OrderEntityState implements Jsonable {
    @JsonIgnore
    public static final OrderEntityState EMPTY = new OrderEntityState(null);
    private Order order;

    public boolean isEmpty() {
        return order == null;
    }

}
