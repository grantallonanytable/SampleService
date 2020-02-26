package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

/**
 * Дата и временной сдвиг таймзоны для записи в БД.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED, onConstructor = @__({@Deprecated}))
@AllArgsConstructor
@Embeddable
class OrderDateTime {
    private LocalDateTime dateTime;
    @Convert(converter = ZoneOffsetConverter.class)
    private ZoneOffset offset;

}
