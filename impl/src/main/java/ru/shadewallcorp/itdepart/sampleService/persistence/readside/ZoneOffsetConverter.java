package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import java.time.ZoneOffset;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Чтение/запись таймзоны поля БД.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Converter
public class ZoneOffsetConverter implements AttributeConverter<ZoneOffset, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ZoneOffset attribute) {
        if (attribute != null) {
            return attribute.getTotalSeconds();
        }
        return null;
    }

    @Override
    public ZoneOffset convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return ZoneOffset.ofTotalSeconds(dbData);
    }
}
