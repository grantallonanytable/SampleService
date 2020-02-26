package ru.shadewallcorp.itdepart.sampleService.elastic;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Характеристика catalogId продукта (ElasticSearch).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class CatalogId {
    private final String productId;
    private final String skuId;
    private final String serialId;

}
