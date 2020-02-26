package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;

import javax.money.MonetaryAmount;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import static lombok.AccessLevel.PROTECTED;

/**
 * Характеристика продукта для записи в БД.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Builder
@Embeddable
class SkuSubItem {
    private String commerceId;
    @Embedded
    private CatalogId catalogId;
    private String type;
    private String frontName;
    private Integer amount;
    @Columns(columns = {
            @Column(name = "price_currency", nullable = false),
            @Column(name = "price_amount", nullable = false)
    })
    @Type(type = PersistentMoneyAmountAndCurrency.NAME)
    private MonetaryAmount price;
    @Columns(columns = {
            @Column(name = "salePrice_currency", nullable = false),
            @Column(name = "salePrice_amount", nullable = false)
    })
    @Type(type = PersistentMoneyAmountAndCurrency.NAME)
    private MonetaryAmount salePrice;

}
