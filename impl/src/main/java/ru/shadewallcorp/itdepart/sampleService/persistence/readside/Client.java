package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
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
 * Информация о клиенте.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "ORDER_CLIENT")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_gen")
    @SequenceGenerator(name = "delivery_gen", sequenceName = "DELIVERY_SEQ")
    @Column(name = "ID")
    private BigInteger id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "GENDER")
    private String gender;
    @Column(name = "PHONE")
    private String phone;
    @Column(name = "EMAIL")
    private String email;
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "IDENT_TYPE")),
            @AttributeOverride(name = "series", column = @Column(name = "IDENT_SERIES")),
            @AttributeOverride(name = "number", column = @Column(name = "IDENT_NUMBER")),
            @AttributeOverride(name = "issuedBy", column = @Column(name = "IDENT_ISSUEDBY")),
            @AttributeOverride(name = "issuedOn", column = @Column(name = "IDENT_ISSUEDON")),
            @AttributeOverride(name = "issuedCode", column = @Column(name = "IDENT_ISSUEDCODE")),
            @AttributeOverride(name = "birthday", column = @Column(name = "IDENT_BIRTHDAY")),
            @AttributeOverride(name = "placeOfBirth", column = @Column(name = "IDENT_PLACEOFBIRTH"))
    })
    private IdentityCard identityCard;
    @AttributeOverrides({
            @AttributeOverride(name = "postalCode", column = @Column(name = "ADDRESS_POSTALCODE", insertable = false, updatable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "ADDRESS_CITY")),
            @AttributeOverride(name = "street", column = @Column(name = "ADDRESS_STREET")),
            @AttributeOverride(name = "building", column = @Column(name = "ADDRESS_BUILDING")),
            @AttributeOverride(name = "apartment", column = @Column(name = "ADDRESS_APARTMENT"))
    })
    private Address address;
}
