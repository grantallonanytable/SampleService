package ru.shadewallcorp.itdepart.sampleService.elastic;

import lombok.Getter;
import org.taymyr.lagom.elasticsearch.search.dsl.SearchResult;

/**
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Getter
public class OrderSearchResult extends SearchResult<Order> {
}
