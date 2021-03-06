package ru.shadewallcorp.itdepart.sampleService.elastic;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate issuedOn;
    private final String issuedCode;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate birthday;
    private final String placeOfBirth;
}
