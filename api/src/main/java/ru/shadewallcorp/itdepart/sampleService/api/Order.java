package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import javax.money.MonetaryAmount;

/**
 * Информация о заказе (для API сервиса).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class Order {

    private final String id;
    private final ZonedDateTime creationDate;
    private final String state;
    private final String type;
    private final String siteId;
    private final String clientName;
    private final MonetaryAmount totalPrice;
    private final Payment payment;
    private final String comment;
    /** Номер для переноса. */
    private final String mnp;
    private final Delivery delivery;

}
