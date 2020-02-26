package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Адрес.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class Address {
    private final String postalCode;
    private final String city;
    private final String street;
    private final String building;
    private final String apartment;
}
