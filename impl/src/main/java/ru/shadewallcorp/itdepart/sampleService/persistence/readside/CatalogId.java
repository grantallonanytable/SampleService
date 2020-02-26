package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

/**
 * Характеристика catalogId продукта для записи в БД.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Embeddable
@Builder
class CatalogId {
    private String productId;
    private String skuId;
    private String serialId;

}
