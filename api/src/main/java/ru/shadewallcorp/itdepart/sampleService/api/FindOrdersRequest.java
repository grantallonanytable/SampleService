package ru.shadewallcorp.itdepart.sampleService.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 *  Параметры для поиска заказов.
 * <br>Сходный набор полей у заказа.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Builder
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class FindOrdersRequest {

    private String id;
    private ZonedDateTime dateFrom;
    private ZonedDateTime dateTo;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private String state;
    private String type;
    private String siteId;
    private String cityId;
    private String salePointId;
    private Integer shipGroupType;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String paymentType;
    /** Набор допустимых состояний. */
    private String paymentState;
    private String comment;
    private String trackNumber;
    /** Фильтр на поля состава заказа. */
    private String orderComposition;
    private String parentNetwork;
    private String network;
    private String partner;
    private String cancelReason;
    /** Фильтр по заполненности: true - с указанным mnp, false - без mnp, null - все. */
    private Boolean checkMnp;
    /** Фильтр по заполненности:  true - с указанным promo, false - без promo, null - все. */
    private Boolean checkPromo;
    /** Фильтр по delivery.id. */
    private String deliveryId;
    private Integer page;
    private Integer pageSize;

}
