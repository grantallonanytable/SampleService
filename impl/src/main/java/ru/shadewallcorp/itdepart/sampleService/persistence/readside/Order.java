package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;

import java.util.List;
import javax.money.MonetaryAmount;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static javax.persistence.FetchType.EAGER;
import static lombok.AccessLevel.PROTECTED;

/**
 * Поля заказа для записи в БД.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 * @see <a href="https://docs.jboss.org/hibernate/annotations/3.5/reference/en/html/entity.html">Hibernate.Annotations (package-level!)</a>
 */
@Data
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "ORDERS")
@TypeDef(name = PersistentMoneyAmountAndCurrency.NAME,
        typeClass = PersistentMoneyAmountAndCurrency.class,
        defaultForType = MonetaryAmount.class)
public class Order {
    @Id
    @Column(name = "ID")
    private String id;
    @AttributeOverrides({
            @AttributeOverride(name = "dateTime", column = @Column(name = "CREATION_DATE", nullable = false)),
            @AttributeOverride(name = "offset", column = @Column(name = "CREATION_DATE_OFFSET"))
    })
    private OrderDateTime creationDate;
    @Column(name = "STATE")
    private String state;
    @Column(name = "ORDER_TYPE")
    private String type;
    @Column(name = "SITE_ID")
    private String siteId;
    @Columns(columns = {
            @Column(name = "TOTAL_PRICE_CURRENCY", nullable = false),
            @Column(name = "TOTAL_PRICE_AMOUNT", nullable = false)
    })
    private MonetaryAmount totalPrice;
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "PAY_TYPE")),
            @AttributeOverride(name = "state", column = @Column(name = "PAY_STATE"))
    })
    private Payment payment;
    @Column(name = "COMMENTARY")
    private String comment;
    @Column(name = "PROMO")
    private String promo;
    @Column(name = "PARENT_NETWORK")
    private String parentNetwork;
    @Column(name = "NETWORK")
    private String network;
    @Column(name = "PARTNER")
    private String partner;
    @Column(name = "CANCEL_REASON")
    private String cancelReason;
    @Column(name = "MNP")
    private String mnp;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(
            name = "ORDER_ITEM",
            joinColumns = @JoinColumn(name = "ORDER_ID")
    )
    private List<OrderCompositionItem> items;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "DELIVERY_ID")
    private Delivery delivery;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLIENT_ID")
    private Client client;

}
