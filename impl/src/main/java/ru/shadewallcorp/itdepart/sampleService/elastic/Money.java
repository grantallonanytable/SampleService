package ru.shadewallcorp.itdepart.sampleService.elastic;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * "Moneta" для ES.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Value
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Builder
public class Money {
    private final BigDecimal amount;
    private final String currency;

}
