package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Элемент списка состава заказа для persistent entity.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class OrderCompositionItem implements Jsonable {
    private final SkuItem item;
    private final SkuSubItem subItem;
    private final String itemState;

}
