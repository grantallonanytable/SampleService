package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import javax.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

/**
 * Дополнительная информация о клиенте.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@Builder
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Embeddable
public class IdentityCard {
    private String type;
    private String series;
    private String number;
    private String issuedBy;
    private LocalDate issuedOn;
    private String issuedCode;
    private LocalDate birthday;
    private String placeOfBirth;
}
