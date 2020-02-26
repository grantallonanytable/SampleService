package ru.shadewallcorp.itdepart.sampleService.elastic;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.elasticsearch.search.dsl.query.Query;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.DateRange;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.RangeQuery;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Утилитарный класс со списком полей для поиска.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
final class SearchTermsUtil {

    private SearchTermsUtil() {
    }

    /**
     *  Добавить опциональный параметр в список параметров для поиска.
     *  Не включаются в поиск пустые значения и строки, состоящие из пробельных символов.
     */
    static <V> void addQueryIfValueNotBlank(@NotNull List<Query> list, @NotNull BiFunction<String, String, Query> queryConstructor,
                                            String fieldName, V fieldValue) {
        if ((fieldValue != null) && StringUtils.isNotBlank(fieldValue.toString())) {
            list.add(queryConstructor.apply(fieldName, fieldValue.toString()));
        }
    }

    /**
     *  Добавить опциональный параметр в список параметров для поиска по диапазону.
     *  Не включаются в поиск пустые значения и строки, состоящие из пробельных символов.
     */
    static <V, Q> void addQueryIfValueNotBlankRange(@NotNull List<Query> list, V valueFrom, V valueTo,
                                                            @NotNull BiFunction<V, V, Q> getParamValueFunc,
                                                            @NotNull Function<Q, Query> queryConvertFunc) {
        if ((valueFrom != null) || (valueTo != null)) {
            list.add(queryConvertFunc.apply(getParamValueFunc.apply(valueFrom, valueTo)));
        }
    }

    /**
     * Поддержка поиска по полю date (dateTo-dateFrom).
     * Даты тут хранятся без часового пояса, чтобы избежать ошибки "[range] query does not support [offset]".
     * Соответствие часовых поясов у этих параметров проверять отдельно.
     */
    static class DateTermRangeUtils {

        static DateRange getRange(ZonedDateTime gte, ZonedDateTime lte) {
            DateRange.ZonedDateTimeRangeBuilder builder = DateRange.zonedDateTimeBuilder();
            if (gte != null) {
                builder.gte(gte);
            }
            if (lte != null) {
                builder.lte(lte);
            }
            return builder.build();
        }

        static RangeQuery ofRange(DateRange range) {
            return new RangeQuery("creationDate", range);
        }

    }

}
