package ru.shadewallcorp.itdepart.sampleService.topic;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Базовый класс событий, получаемых из Kafka.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "eventType",
        defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitOrder.class)
})
public abstract class OrderEvent {
}
