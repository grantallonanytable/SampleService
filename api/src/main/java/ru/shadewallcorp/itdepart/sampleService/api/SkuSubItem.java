package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.Builder;
import lombok.Value;

import javax.money.MonetaryAmount;

/**
 * Характеристика продукта (API сервиса заказов).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@Builder
public class SkuSubItem {
    private final String commerceId;
    private final CatalogId catalogId;
    private final String type;
    private final String frontName;
    private final Integer amount;
    private final MonetaryAmount price;
    private final MonetaryAmount salePrice;

}
