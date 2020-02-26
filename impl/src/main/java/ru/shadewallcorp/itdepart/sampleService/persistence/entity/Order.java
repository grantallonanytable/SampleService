package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.pcollections.PSequence;

import java.time.ZonedDateTime;
import javax.money.MonetaryAmount;

/**
 * Поля заказа для persistent entity.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class Order implements Jsonable {

    private final String id;
    private final ZonedDateTime creationDate;
    private final String state;
    private final String type;
    private final String siteId;
    private final MonetaryAmount totalPrice;
    private final Payment payment;
    private final String comment;
    private final PSequence<OrderCompositionItem> items;
    private final String promo;
    private final String parentNetwork;
    private final String network;
    private final String partner;
    private final String cancelReason;
    /** Номер для переноса. */
    private final String mnp;
    private final Delivery delivery;
    private final Client client;

}
