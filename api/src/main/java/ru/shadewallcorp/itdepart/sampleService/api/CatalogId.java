package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.Builder;
import lombok.Value;

/**
 * Характеристика catalogId продукта (API сервиса заказов).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@Builder
public class CatalogId {
    private final String productId;
    private final String skuId;
    private final String serialId;

}
