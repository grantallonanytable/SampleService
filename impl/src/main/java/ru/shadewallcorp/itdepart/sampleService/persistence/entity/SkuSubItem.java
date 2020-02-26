package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.money.MonetaryAmount;

/**
 * Характеристика продукта для persistent entity.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class SkuSubItem implements Jsonable {
    private String commerceId;
    private CatalogId catalogId;
    private String type;
    private String frontName;
    private Integer amount;
    private MonetaryAmount price;
    private MonetaryAmount salePrice;

}
