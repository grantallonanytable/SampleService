package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.pcollections.PSequence;

import java.time.ZonedDateTime;
import javax.money.MonetaryAmount;

/**
 * Карточка заказа (для API сервиса).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public final class OrderCard {

    private final String id;
    private final ZonedDateTime creationDate;
    private final String state;
    private final String type;
    private final String siteId;
    private final MonetaryAmount totalPrice;
    private final Payment payment;
    private final String comment;
    /** Номер для переноса. */
    private final String mnp;
    private final PSequence<OrderCompositionItem> items;
    private final String promo;
    private final String parentNetwork;
    private final String network;
    private final String partner;
    private final String cancelReason;
    private final DeliveryCard deliveryCard;
    private final Client client;

}
