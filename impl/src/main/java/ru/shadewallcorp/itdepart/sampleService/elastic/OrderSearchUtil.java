package ru.shadewallcorp.itdepart.sampleService.elastic;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.taymyr.lagom.elasticsearch.script.Script;
import org.taymyr.lagom.elasticsearch.search.dsl.query.Query;
import org.taymyr.lagom.elasticsearch.search.dsl.query.fulltext.MatchPhrasePrefixQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.fulltext.MatchPhraseQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.script.ScriptQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.ExistsQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.PrefixQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.TermQuery;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersRequest;
import ru.shadewallcorp.itdepart.sampleService.elastic.SearchTermsUtil.DateTermRangeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ru.shadewallcorp.itdepart.sampleService.elastic.SearchTermsUtil.addQueryIfValueNotBlank;
import static ru.shadewallcorp.itdepart.sampleService.elastic.SearchTermsUtil.addQueryIfValueNotBlankRange;

/**
 * Утилитарный класс со списком полей для поиска.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
final class OrderSearchUtil {

    private static final String SCRIPT_REQUEST_AND = " && ";
    private static final String SCRIPT_REQUEST_SUBSTRING_ID = "params._source['id'].indexOf(params.id)>=0";
    private static final String SCRIPT_REQUEST_WITH_DATE_INTERVAL_GET =
            "!LocalDate.parse(params.min).isAfter(ZonedDateTime.parse(params._source['creationDate']).toLocalDate())";
    private static final String SCRIPT_REQUEST_WITH_DATE_INTERVAL_LT =
            "LocalDate.parse(params.max).isAfter(ZonedDateTime.parse(params._source['creationDate']).toLocalDate())";
    private static final String SCRIPT_REQUEST_WITH_TIME_INTERVAL_GET =
            "!LocalTime.parse(params.min).isAfter(ZonedDateTime.parse(params._source['creationDate']).toLocalTime())";
    private static final String SCRIPT_REQUEST_WITH_TIME_INTERVAL_LT =
            "LocalTime.parse(params.max).isAfter(ZonedDateTime.parse(params._source['creationDate']).toLocalTime())";

    private OrderSearchUtil() {
        //
    }

    /**
     * Получить список параметров для поиска.
     * Поля для поиска по полному совпадению терма/ключевого слова.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getTermFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> terms = new ArrayList<>();
        addQueryIfValueNotBlank(terms, TermQuery::of, "siteId", findOrdersRequest.getSiteId());
        addQueryIfValueNotBlank(terms, TermQuery::of, "state", findOrdersRequest.getState());
        addQueryIfValueNotBlank(terms, TermQuery::of, "type", findOrdersRequest.getType());
        addQueryIfValueNotBlank(terms, TermQuery::of, "delivery.cityId", findOrdersRequest.getCityId());
        addQueryIfValueNotBlank(terms, TermQuery::of, "delivery.shipGroupType", findOrdersRequest.getShipGroupType());
        addQueryIfValueNotBlank(terms, TermQuery::of, "delivery.salePoint.id", findOrdersRequest.getSalePointId());
        addQueryIfValueNotBlank(terms, TermQuery::of, "payment.type", findOrdersRequest.getPaymentType());
        addQueryIfValueNotBlank(terms, TermQuery::of, "payment.state", findOrdersRequest.getPaymentState());
        addQueryIfValueNotBlank(terms, TermQuery::of, "cancelReason", findOrdersRequest.getCancelReason());
        // Параметры страниц передаются отдельно
        return terms;
    }

    /**
     * Получить список параметров для поиска.
     * Поля для поиска по полному совпадению id.
     *
     * @param id id заказа
     * @return список фильтров
     */
    @NotNull
    static List<Query> getTermIdFilter(@NotNull Long id) {
        return Collections.singletonList(TermQuery.of("id", id.toString()));
    }

    /**
     * Получить список параметров для поиска.
     * Поля для поиска по частичному совпадению терма/ключевого слова.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getPrefixFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> prefixes = new ArrayList<>();
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "delivery.id", findOrdersRequest.getDeliveryId());
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "delivery.trackNumber", findOrdersRequest.getTrackNumber());
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "client.email", findOrdersRequest.getClientEmail());
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "client.phone", findOrdersRequest.getClientPhone());
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "parentNetwork", findOrdersRequest.getParentNetwork());
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "network", findOrdersRequest.getNetwork());
        addQueryIfValueNotBlank(prefixes, PrefixQuery::of, "partner", findOrdersRequest.getPartner());
        // Параметры страниц передаются отдельно
        return prefixes;
    }

    /**
     * Получить список параметров для поиска.
     * Поля для поиска по частичному совпадению текста.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getMatchFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> matches = new ArrayList<>();
        addQueryIfValueNotBlank(matches, MatchPhrasePrefixQuery::of, "client.name", findOrdersRequest.getClientName());
        addQueryIfValueNotBlank(matches, MatchPhrasePrefixQuery::of, "comment", findOrdersRequest.getComment());
        return matches;
    }

    /**
     * Получить список параметров для поиска.
     * Поля для поиска по диапазону.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getRangeFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> ranges = new ArrayList<>();
        addQueryIfValueNotBlankRange(ranges, findOrdersRequest.getDateFrom(), findOrdersRequest.getDateTo(),
                DateTermRangeUtils::getRange, DateTermRangeUtils::ofRange);
        // Параметры страниц передаются отдельно
        return ranges;
    }

    /**
     * Получить объект-скрипт проверки по диапазону значений.
     *
     * @param scriptGET строка скрипта проверки левой границы диапазона (сравнение через >=)
     * @param scriptLT  строка скрипта проверки правой границы диапазона (сравнение через <)
     * @param valueFrom (опционально) значение левой границы
     * @param valueTo   (опционально) значение правой границы
     * @return объект скрипта для запроса в elastic search или null если обе границы не заданы
     */
    private static <V> ScriptQuery getRangeScriptQuery(@NotNull String scriptGET, @NotNull String scriptLT, V valueFrom, V valueTo) {
        ScriptQuery res = null;
        StringBuilder script = new StringBuilder();
        Map<String, V> params = new TreeMap<>();
        if (valueFrom != null) {
            script.append(scriptGET);
            params.put("min", valueFrom);
        }
        if (valueTo != null) {
            if (script.length() > 0) {
                script.append(SCRIPT_REQUEST_AND);
            }
            script.append(scriptLT);
            params.put("max", valueTo);
        }
        if (script.length() > 0) {
            res = new ScriptQuery(Script.builder()
                    .source(script.toString())
                    .params(params)
                    .build());
        }
        return res;
    }

    /**9
     * Получить запрос для поиска с использованием скрипта.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getScriptFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> res = new ArrayList<>();
        ScriptQuery scriptQuery;
        // По частичному совпадению id (в том числе по последним цифрам id)
        final String paramId = findOrdersRequest.getId();
        if (StringUtils.isNotBlank(paramId)) {
            res.add(new ScriptQuery(Script.builder()
                    .source(SCRIPT_REQUEST_SUBSTRING_ID)
                    .param("id", paramId)
                    .build()));
        }
        // По дате без учета TZ

        String dateFrom = findOrdersRequest.getDateFrom() != null ? findOrdersRequest.getDateFrom().toLocalDate().toString() : null;
        String dateTo = findOrdersRequest.getDateTo() != null ? findOrdersRequest.getDateTo().toLocalDate().toString() : null;
        scriptQuery = getRangeScriptQuery(SCRIPT_REQUEST_WITH_DATE_INTERVAL_GET, SCRIPT_REQUEST_WITH_DATE_INTERVAL_LT, dateFrom, dateTo);
        if (scriptQuery != null) {
            res.add(scriptQuery);
        }
        // По времени в рамках каджого дня
        String timeFrom = findOrdersRequest.getTimeFrom() != null ? findOrdersRequest.getTimeFrom().toString() : null;
        String timeTo = findOrdersRequest.getTimeTo() != null ? findOrdersRequest.getTimeTo().toString() : null;
        scriptQuery = getRangeScriptQuery(SCRIPT_REQUEST_WITH_TIME_INTERVAL_GET, SCRIPT_REQUEST_WITH_TIME_INTERVAL_LT, timeFrom, timeTo);
        if (scriptQuery != null) {
            res.add(scriptQuery);
        }
        return res;
    }

    /**
     * Получить список параметров для поиска.
     * Поиск по признаку заполненности полей.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getExistFlagsFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> exists = new ArrayList<>();
        if (findOrdersRequest.getCheckMnp() != null && findOrdersRequest.getCheckMnp()) {
            exists.add(ExistsQuery.of("mnp"));
        }
        if (findOrdersRequest.getCheckPromo() != null && findOrdersRequest.getCheckPromo()) {
            exists.add(ExistsQuery.of("promo"));
        }
        return exists;
    }

    /**
     * Получить список параметров для поиска.
     * Поиск по признаку незаполненности полей.
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getNonExistFlagsFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> nonExists = new ArrayList<>();
        if (findOrdersRequest.getCheckMnp() != null && !findOrdersRequest.getCheckMnp()) {
            nonExists.add(ExistsQuery.of("mnp"));
        }
        if (findOrdersRequest.getCheckPromo() != null && !findOrdersRequest.getCheckPromo()) {
            nonExists.add(ExistsQuery.of("promo"));
        }
        return nonExists;
    }

    /**
     * Получить список параметров для поиска.
     * Поиск по вложенным полям.
     * Если несколько терминов - поиск по терминам (с учетом семантики),
     * если одно слово - поиск по подстроке
     *
     * @param findOrdersRequest параметры поиска
     * @return список фильтров
     */
    @NotNull
    static List<Query> getMultiMatchFromFilter(@NotNull FindOrdersRequest findOrdersRequest) {
        List<Query> match = new ArrayList<>();
        String[] orderCompositionFilterTerms = StringUtils.split(findOrdersRequest.getOrderComposition(), " ");
        boolean isMultiTermFiler = (orderCompositionFilterTerms != null) && (orderCompositionFilterTerms.length > 1);
        addQueryIfValueNotBlank(match, isMultiTermFiler ? MatchPhraseQuery::of : MatchPhrasePrefixQuery::of,
                "orderCompositionAggregated", findOrdersRequest.getOrderComposition());
        return match;
    }

}
