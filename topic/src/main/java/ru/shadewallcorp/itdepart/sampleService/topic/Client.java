package ru.shadewallcorp.itdepart.sampleService.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Информация о клиенте.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class Client {
    private final String name;
    private final String gender;
    private final String phone;
    private final String email;
    private final IdentityCard identityCard;
    private final Address address;
}
