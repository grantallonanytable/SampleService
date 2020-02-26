package ru.shadewallcorp.itdepart.sampleService.elastic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Описание тарифной зоны.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class TariffZone {
    private final String id;
    private final String name;
    /* Описание срока доставки: '1-2 дня' */
    private final String deliveryTimeDesc;
    /* Стоимость доставки */
    private final Money cost;
}
