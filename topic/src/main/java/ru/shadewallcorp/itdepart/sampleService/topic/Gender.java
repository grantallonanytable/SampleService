package ru.shadewallcorp.itdepart.sampleService.topic;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Пол.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
public enum Gender {
    MALE("male"),
    FEMALE("female");

    @Getter(onMethod = @__({@JsonValue}))
    private String value;
}
