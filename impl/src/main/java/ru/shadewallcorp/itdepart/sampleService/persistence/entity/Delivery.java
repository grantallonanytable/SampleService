package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Краткая информация о доставке.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class Delivery {
    private final String id;
    private final String name;
    private final String alias;
    private final Integer shipGroupType;
    private final String cityId;
    private final String trackNumber;
    private final TariffZone tariffZone;
    private final SalePoint salePoint;
    private final Address courierInfo;
}
