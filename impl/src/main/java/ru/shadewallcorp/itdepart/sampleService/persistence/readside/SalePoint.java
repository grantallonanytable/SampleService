package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

/**
 * Точка доставки, информация при самовывозе.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Embeddable
@Builder
public class SalePoint {
    String id;
    String address;
}
