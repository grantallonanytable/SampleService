package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Точка доставки, информация при самовывозе.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class SalePoint {
    String id;
    String address;
}
