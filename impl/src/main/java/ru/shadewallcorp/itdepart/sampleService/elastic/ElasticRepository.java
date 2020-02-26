package ru.shadewallcorp.itdepart.sampleService.elastic;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import org.taymyr.lagom.elasticsearch.ServiceCall;
import org.taymyr.lagom.elasticsearch.document.ElasticDocument;
import org.taymyr.lagom.elasticsearch.document.dsl.update.DocUpdateRequest;
import org.taymyr.lagom.elasticsearch.search.ElasticSearch;
import org.taymyr.lagom.elasticsearch.search.dsl.HitResult;
import org.taymyr.lagom.elasticsearch.search.dsl.SearchRequest;
import org.taymyr.lagom.elasticsearch.search.dsl.query.Query;
import org.taymyr.lagom.elasticsearch.search.dsl.query.compound.BoolQuery;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersRequest;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersResponse;
import ru.shadewallcorp.itdepart.sampleService.api.OrderCard;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import static akka.Done.done;
import static java.math.BigDecimal.ROUND_UP;
import static java.util.Optional.ofNullable;

/**
 * elastic elastic.
 * Used to retrieve information about orders.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Singleton
public class ElasticRepository {

    public static final String ORDER_INDEX = "order";
    public static final String ORDER_TYPE = "order";
    private static final int DEFALUT_ES_PAGE_SIZE = 10;
    private static final int DEFAULT_ES_FROM = 0;
    private final ElasticDocument elasticDocument;
    private final ElasticSearch elasticSearch;

    @Inject
    public ElasticRepository(ElasticDocument elasticDocument, ElasticSearch elasticSearch) {
        this.elasticDocument = elasticDocument;
        this.elasticSearch = elasticSearch;
    }

    /**
     * Поиск закаов по индексу.
     *
     * @param findOrdersRequest параметры поиска
     * @return {@code CompletionStage<List<Order>>}
     */
    public CompletionStage<FindOrdersResponse> findOrders(FindOrdersRequest findOrdersRequest) {
        return findOrdersByRequest(getFindOrdersRequest(findOrdersRequest));
    }

    /**
     * Вычислить количество страниц в результате на основе количества записей.
     * @param recordCount количество записей, которое возвращает эластик
     * @param pageSize размер страницы, передаваемый в запросе
     * @return количество страниц в результате
     */
    private static int calcTotalPageCount(Integer recordCount, Integer pageSize) {
        return BigDecimal.valueOf(recordCount, 0)
                .divide(BigDecimal.valueOf(pageSize != null ? pageSize : DEFALUT_ES_PAGE_SIZE, 0), ROUND_UP).intValue();
    }

    protected CompletionStage<FindOrdersResponse> findOrdersByRequest(SearchRequest searchRequest) {
        return ServiceCall.invoke(elasticSearch.search(ORDER_INDEX, ORDER_INDEX), searchRequest, OrderSearchResult.class)
                .thenApply(result -> ofNullable(result)
                        .map(OrderSearchResult::getHits)
                        .map(hits -> Converters.ESToApi.toFindOrdersResponse(hits.getHits()
                                    .stream()
                                    .map(HitResult::getSource)
                                    .collect(Collectors.toList()),
                            calcTotalPageCount(hits.getTotal().getValue(), searchRequest.getSize()))
                        )
                        .orElse(Converters.ESToApi.toFindOrdersResponse(null, 0)));
    }

    /**
     * Поиск закаов по индексу.
     *
     * @param id идентификатор заказа
     * @return {@code CompletionStage<List<OrderCard>>}
     */
    public CompletionStage<OrderCard> findOrderCards(Long id) {
        return findOrderCardsByRequest(getFindOrderCardsRequest(id));
    }

    private CompletionStage<OrderCard> findOrderCardsByRequest(SearchRequest searchRequest) {
        return ServiceCall.invoke(elasticSearch.search(ORDER_INDEX, ORDER_INDEX), searchRequest, OrderSearchResult.class)
                .thenApply(result -> ofNullable(result)
                        .map(OrderSearchResult::getHits)
                        .map(hits -> hits.getHits()
                                .stream()
                                .map(hit -> Converters.ESToApi.toOrderCard(hit.getSource()))
                        )
                        .orElseThrow(() -> new NotFound("Order card not found!"))
                        .findFirst()
                        .orElseThrow(() -> new NotFound("Order card not found!")));
    }

    private SearchRequest getFindOrderCardsRequest(Long id) {
        // Поиск по полному совпадению id
        BoolQuery.Builder queryBuilder = BoolQuery.builder();
        queryBuilder.filter(OrderSearchUtil.getTermIdFilter(id));
        // TODO проверка на null
        return new SearchRequest(queryBuilder.build());
    }

    protected SearchRequest getFindOrdersRequest(FindOrdersRequest findOrdersRequest) {
        // Добавить поля для поиска по полному совпадению терма/ключевого слова
        List<Query> filter = OrderSearchUtil.getTermFromFilter(findOrdersRequest);
        // Добавить поля для поиска по частичному совпадению терма/ключевого слова
        filter.addAll(OrderSearchUtil.getPrefixFromFilter(findOrdersRequest));
        // Добавить поля для поиска по частичному совпадению текста
        filter.addAll(OrderSearchUtil.getMatchFromFilter(findOrdersRequest));
        // Добавить поля для поиска с использованием скриптов
        filter.addAll(OrderSearchUtil.getScriptFromFilter(findOrdersRequest));
        // Добавить поиск по составу заказа
        filter.addAll(OrderSearchUtil.getMultiMatchFromFilter(findOrdersRequest));
        BoolQuery.Builder queryBuilder = BoolQuery.builder();
        queryBuilder.filter(filter);
        // Добавить признаки заполненности / незаполненности полей
        List<Query> existFlags = OrderSearchUtil.getExistFlagsFromFilter(findOrdersRequest);
        queryBuilder.must(existFlags);
        List<Query> nonExistFlags = OrderSearchUtil.getNonExistFlagsFromFilter(findOrdersRequest);
        queryBuilder.mustNot(nonExistFlags);
        return new SearchRequest(queryBuilder.build(),
                null,
                findOrdersRequest.getPage() != null ?
                        findOrdersRequest.getPage() * (findOrdersRequest.getPageSize() != null ?
                                findOrdersRequest.getPageSize() :
                                DEFALUT_ES_PAGE_SIZE) :
                        DEFAULT_ES_FROM,
                findOrdersRequest.getPageSize());
    }

    /**
     * Получить карточку заказа по ID.
     * @param id ID
     * @return {@code CompletionStage<Done>}
     */
    public CompletionStage<OrderCard> getOrder(Long id) {
        return ServiceCall.invoke(
                elasticDocument.getSource(ORDER_INDEX, ORDER_TYPE, id.toString()), NotUsed.getInstance(), OrderCardSearchResult.class)
                .thenApply(result -> ofNullable(result)
                        .map(orderCardSearchResult -> orderCardSearchResult.getTyped("", OrderCard.class))
                        .map(Collections::singletonList)
                        .map(Collection::stream).orElse(Stream.empty())
                        .findFirst()
                        .orElseThrow(() -> new NotFound("Order card not found!")));
    }

    /**
     * Сохранить данные заказа в ES.
     *
     * @param orderES заказ в формате эластика
     * @return {@code CompletionStage<Done>}
     */
    public CompletionStage<Done> storeOrder(Order orderES) {
        DocUpdateRequest request =
                DocUpdateRequest.builder().doc(orderES).docAsUpsert(true).build();
        return ServiceCall.invoke(elasticDocument.update(ORDER_INDEX, ORDER_TYPE, orderES.getId()), request)
                .thenApply(updateResult -> done());
    }

}
