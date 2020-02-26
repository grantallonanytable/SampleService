package ru.shadewallcorp.itdepart.sampleService.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * Дополнительная информация о клиенте.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class IdentityCard {
    private final String type;
    private final String series;
    private final String number;
    private final String issuedBy;
    private final LocalDate issuedOn;
    private final String issuedCode;
    private final LocalDate birthday;
    private final String placeOfBirth;
}
