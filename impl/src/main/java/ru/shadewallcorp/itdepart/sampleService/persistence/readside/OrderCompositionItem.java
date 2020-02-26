package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import static lombok.AccessLevel.PROTECTED;

/**
 * Элемент списка состава заказа для записи в БД.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@Builder
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Embeddable
public class OrderCompositionItem {

    @Column(name = "ITEM_STATE")
    private String itemState;

    @AttributeOverrides({
            @AttributeOverride(name = "commerceId", column = @Column(name = "ITEM_COMMERCE_ID")),
            @AttributeOverride(name = "catalogId.productId", column = @Column(name = "ITEM_CATALOG_PRODUCT_ID")),
            @AttributeOverride(name = "catalogId.skuId", column = @Column(name = "ITEM_CATALOG_SKU_ID")),
            @AttributeOverride(name = "catalogId.serialId", column = @Column(name = "ITEM_CATALOG_SERIAL_ID")),
            @AttributeOverride(name = "type", column = @Column(name = "ITEM_TYPE")),
            @AttributeOverride(name = "frontName", column = @Column(name = "ITEM_FRONT_NAME")),
            @AttributeOverride(name = "mnp", column = @Column(name = "ITEM_MNP")),
            @AttributeOverride(name = "amount", column = @Column(name = "ITEM_AMOUNT")),
            @AttributeOverride(name = "price", column = @Column(name = "ITEM_PRICE_CURRENCY")),
            @AttributeOverride(name = "price", column = @Column(name = "ITEM_PRICE_AMOUNT")),
            @AttributeOverride(name = "salePrice", column = @Column(name = "ITEM_SALEPRICE_CURRENCY")),
            @AttributeOverride(name = "salePrice", column = @Column(name = "ITEM_SALEPRICE_AMOUNT"))
    })
    @Embedded
    private SkuItem item;

    @AttributeOverrides({
            @AttributeOverride(name = "commerceId", column = @Column(name = "SUBITEM_COMMERCE_ID")),
            @AttributeOverride(name = "catalogId.productId", column = @Column(name = "SUBITEM_CATALOG_PRODUCT_ID")),
            @AttributeOverride(name = "catalogId.skuId", column = @Column(name = "SUBITEM_CATALOG_SKU_ID")),
            @AttributeOverride(name = "catalogId.serialId", column = @Column(name = "SUBITEM_CATALOG_SERIAL_ID")),
            @AttributeOverride(name = "type", column = @Column(name = "SUBITEM_TYPE")),
            @AttributeOverride(name = "frontName", column = @Column(name = "SUBITEM_FRONT_NAME")),
            @AttributeOverride(name = "amount", column = @Column(name = "SUBITEM_AMOUNT")),
            @AttributeOverride(name = "price", column = @Column(name = "SUBITEM_PRICE_CURRENCY")),
            @AttributeOverride(name = "price", column = @Column(name = "SUBITEM_PRICE_AMOUNT")),
            @AttributeOverride(name = "salePrice", column = @Column(name = "SUBITEM_SALEPRICE_CURRENCY")),
            @AttributeOverride(name = "salePrice", column = @Column(name = "SUBITEM_SALEPRICE_AMOUNT"))
    })
    @Embedded
    private SkuSubItem subItem;

}
