package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.money.MonetaryAmount;
import javax.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

/**
 * Описание тарифной зоны.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@Builder
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Embeddable
public class TariffZone {
    private String id;
    private String name;
    /* Описание срока доставки: '1-2 дня' */
    private String deliveryTimeDesc;
    /* Стоимость доставки */
    private MonetaryAmount cost;
}
