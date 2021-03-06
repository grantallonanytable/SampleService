package ru.shadewallcorp.itdepart.sampleService.topic;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Элемент списка состава заказа.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class OrderCompositionItem {
    private final SkuItem item;
    private final SkuSubItem subItem;
    private final ItemState itemState;
}
