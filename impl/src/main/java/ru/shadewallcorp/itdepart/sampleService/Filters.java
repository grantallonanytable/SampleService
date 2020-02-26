package ru.shadewallcorp.itdepart.sampleService;

import org.taymyr.lagom.metrics.MetricsFilter;
import play.http.DefaultHttpFilters;

import javax.inject.Inject;

/**
 * Фильтр с метриками.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class Filters extends DefaultHttpFilters {

    @Inject
    public Filters(MetricsFilter filter) {
        super(filter);
    }
}
