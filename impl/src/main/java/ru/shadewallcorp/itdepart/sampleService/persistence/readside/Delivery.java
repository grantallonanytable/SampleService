package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TypeDef;

import java.math.BigInteger;
import javax.money.MonetaryAmount;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static lombok.AccessLevel.PROTECTED;

/**
 * Данные по доставке.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "ORDER_DELIVERY")
@TypeDef(name = PersistentMoneyAmountAndCurrency.NAME,
        typeClass = PersistentMoneyAmountAndCurrency.class,
        defaultForType = MonetaryAmount.class)
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_gen")
    @SequenceGenerator(name = "delivery_gen", sequenceName = "DELIVERY_SEQ")
    @Column(name = "ID")
    private BigInteger id;
    @Column(name = "DELIVERY_ID")
    private String deliveryId;
    @Column(name = "DELIVERY_NAME")
    private String name;
    @Column(name = "DELIVERY_ALIAS")
    private String alias;
    @Column(name = "SHIPGROUP_TYPE")
    private Integer shipGroupType;
    @Column(name = "CITY_ID")
    private String cityId;
    @Column(name = "TRACK_NUMBER")
    private String trackNumber;

    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "TARIFF_ZONE_ID")),
            @AttributeOverride(name = "name", column = @Column(name = "TARIFF_ZONE_NAME")),
            @AttributeOverride(name = "deliveryTimeDesc", column = @Column(name = "TARIFF_ZONE_DESC")),
            @AttributeOverride(name = "cost", column = @Column(name = "TARIFF_ZONE_COST_CURRENCY")),
            @AttributeOverride(name = "cost", column = @Column(name = "TARIFF_ZONE_COST_AMOUNT"))
    })
    private TariffZone tariffZone;

    @AttributeOverrides({
            @AttributeOverride(name = "postalCode", column = @Column(name = "COURIER_INFO_POSTALCODE")),
            @AttributeOverride(name = "city", column = @Column(name = "COURIER_INFO_CITY")),
            @AttributeOverride(name = "street", column = @Column(name = "COURIER_INFO_STREET")),
            @AttributeOverride(name = "building", column = @Column(name = "COURIER_INFO_BUILDING")),
            @AttributeOverride(name = "apartment", column = @Column(name = "COURIER_INFO_APARTMENT"))
    })
    private Address courierInfo;

    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "SALE_POINT_ID")),
            @AttributeOverride(name = "address", column = @Column(name = "SALE_POINT_ADDRESS"))
    })
    private SalePoint salePoint;
}
