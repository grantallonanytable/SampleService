package ru.shadewallcorp.itdepart.sampleService.topic;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Статус элемента заказа.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
public enum ItemState {
    CART_ADDED("CART_ADDED"),
    RESERVED("RESERVED"),
    UNAVAILABLE_TO_RESERVE("UNAVAILABLE_TO_RESERVE"),
    PURCHASED("PURCHASED"),
    CANCEL("CANCEL");

    @Getter(onMethod = @__({@JsonValue}))
    private String value;
}
