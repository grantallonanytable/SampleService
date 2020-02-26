package ru.shadewallcorp.itdepart.sampleService.elastic;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Характеристика продукта (ElasticSearch).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class SkuItem {
    private final String commerceId;
    private final CatalogId catalogId;
    private final String type;
    private final String frontName;
    private final String mnp;
    private final Integer amount;
    private final Money price;
    private final Money salePrice;

}
