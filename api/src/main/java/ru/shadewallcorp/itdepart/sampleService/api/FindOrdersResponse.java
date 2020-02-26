package ru.shadewallcorp.itdepart.sampleService.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.pcollections.PSequence;

/**
 * Результат поиска заказов (API сервиса).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@AllArgsConstructor
@Builder
@Value
public class FindOrdersResponse {
    private PSequence<Order> orders;
    private final Long totalPages;
}
