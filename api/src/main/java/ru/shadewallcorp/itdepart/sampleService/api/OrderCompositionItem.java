package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.Builder;
import lombok.Value;

/**
 * Элемент списка состава заказа (API сервиса заказов).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@Builder
public class OrderCompositionItem {
    private final SkuItem item;
    private final SkuSubItem subItem;
    private final String itemState;

}
