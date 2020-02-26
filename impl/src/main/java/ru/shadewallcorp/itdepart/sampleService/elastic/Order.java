package ru.shadewallcorp.itdepart.sampleService.elastic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Заказ (ElasticSearch).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class Order {

    private final String id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private final ZonedDateTime creationDate;
    private final Integer creationDateOffset;
    private final String state;
    private final String type;
    private final String siteId;
    private final Money totalPrice;
    private final Payment payment;
    private final String comment;
    private final List<OrderCompositionItem> items;
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
