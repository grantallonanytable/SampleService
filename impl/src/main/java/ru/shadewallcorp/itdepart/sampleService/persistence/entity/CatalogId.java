package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Характеристика catalogId продукта для persistent entity.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class CatalogId implements Jsonable {
    private String productId;
    private String skuId;
    private String serialId;

}
