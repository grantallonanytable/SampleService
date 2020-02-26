package ru.shadewallcorp.itdepart.sampleService;

import akka.NotUsed;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.deser.MessageSerializer;
import com.lightbend.lagom.javadsl.api.deser.StrictMessageSerializer;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.testkit.ProducerStub;
import com.lightbend.lagom.javadsl.testkit.ProducerStubFactory;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taymyr.lagom.elasticsearch.ServiceCall;
import org.taymyr.lagom.elasticsearch.deser.ElasticSerializerFactory;
import org.taymyr.lagom.elasticsearch.document.ElasticDocument;
import org.taymyr.lagom.elasticsearch.indices.ElasticIndices;
import org.taymyr.lagom.elasticsearch.search.ElasticSearch;
import org.taymyr.lagom.elasticsearch.search.dsl.HitResult;
import org.taymyr.lagom.elasticsearch.search.dsl.Hits;
import org.taymyr.lagom.elasticsearch.search.dsl.SearchRequest;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersRequest;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersResponse;
import ru.shadewallcorp.itdepart.sampleService.api.Order;
import ru.shadewallcorp.itdepart.sampleService.api.OrderCard;
import ru.shadewallcorp.itdepart.sampleService.application.OrderEventsClient;
import ru.shadewallcorp.itdepart.sampleService.elastic.Converters;
import ru.shadewallcorp.itdepart.sampleService.elastic.ElasticRepository;
import ru.shadewallcorp.itdepart.sampleService.elastic.OrderCardSearchResult;
import ru.shadewallcorp.itdepart.sampleService.elastic.OrderSearchResult;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderEvent;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderEvents;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.topic;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.GEN_ORDER_COUNT;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_CITY_ID;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_CLIENT_PREF;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_COMMENT;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_DELIVERY_ID;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_FRONT;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_FRONT_SUBSTR_TERM_1;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_FRONT_SUBSTR_TERM_2;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_ITEM_STATE_PREFIX;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_SALEPOINT_RUS;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_SITEID;
import static ru.shadewallcorp.itdepart.sampleService.ElasticSearchTestService.TEST_ORDER_SKU_TYPE_SEARCHABLE_PREFIX;

/**
 * Тестирование сервиса заказов.
 * Тесты запускать отдельно (кнопка слева от кода class ElasticSearchTest).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@DisplayName("Test Order Service - ES")
@Disabled
final class ElasticSearchTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchTest.class);

    private static final String S_ASSERT_ORDER_COMP_NOT_FOUND_BY_PREFIX =
            "Не найден заказ по части значения статуса строки состава заказа.";
    private static final String S_ASSERT_ORDER_COMP_NOT_FOUND_BY_WORD_FORM =
            "Неправильно найдены заказы по одному склонению слова в составе заказа.";
    private static final String S_ASSERT_ORDER_COMP_NOT_FOUND_BY_WORD_FORMS =
            "Неправильно найдены заказы по склонениям слова в составе заказа.";
    private static final String S_ASSERT_ORDER_COMP_NOT_FOUND_PHRASE_PREFIX = "Не найден заказ по части значения поля состава заказа item.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_ID_FMT = "Не найден %s по ID.";
    private static final String S_ASSERT_RECORD_FOUND_FAKE_BY_ID_FMT = "Ошибочно найден %s по несуществующему ID.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_SITEID_FMT = "Не найден %s по siteId.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_CITYID_FMT = "Не найден %s по if городff.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_SALEPOINT_FMT = "Не найден %s по точке продаж.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_COMMENT_PART_FMT = "Не найден %s по части комментария.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_CLIENT_PART_FMT = "Не найден %s по части имени клиента.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_DATE_RANGE_FMT = "Не найден %s по диапазону дат.";
    private static final String S_ASSERT_RECORD_NOT_FOUND_BY_DELIVERY_ID_FMT = "Не найден %s по deliveryId.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_FALSE = "Найдено слишком мало записей \"%s\" без поля mnp.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_TRUE = "Найдено слишком мало записей \"%s\" с полем mnp.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_VOID = "Найдено слишком мало записей \"%s\" без учета поля mnp.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_PROMO_FALSE = "Найдено слишком мало записей \"%s\" без поля promo.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_PROMO_TRUE = "Найдено слишком мало записей \"%s\" c полем promo.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_PROMO_VOID = "Найдено слишком мало записей \"%s\" без учета поля promo.";
    private static final String S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_PROMO_TRUE =
            "Найдено слишком мало записей \"%s\" c полями mnp и promo.";
    private static final String S_TYPE_ORDER = "заказ";
    private static final String S_ACCERT_CANT_DESERIALIZE_ES_SEARCH_RESPONSE = "Не удалось десериализовать ответ ES (класс %s).";

    private static ServiceTest.TestServer testServer;
    private static ElasticSearchTestService es;
    private static ProducerStub<OrderEvent> orderProducer;

    private ElasticSearchTest() {
        //
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        // подготовить службы ES
        es = new ElasticSearchTestService();
        ServiceTest.Setup setup = defaultSetup().configureBuilder(
            builder -> builder.overrides(
                    // интерфейсы внешнего или встроенного ES для прямой записи
                    bind(ElasticDocument.class).toInstance(es.getElasticDocument()),
                    bind(ElasticSearch.class).toInstance(es.getElasticSearch()),
                    bind(ElasticIndices.class).toInstance(es.getElasticIndices()),
                    // заглушка для внешнего сервиса
                    bind(OrderEventsClient.class).to(OrderEventsStub.class),
                    // клиент с логированием
                    bind(ElasticRepository.class).to(ElasticRepositoryStub.class)
            )
        )
                .withCassandra()
                .withJdbc();
        testServer = ServiceTest.startServer(setup);
        // Тесты запускать по отдельности, если падает при запуске testServer
        es.startElasticSearchServer();
        // подготовить эластик: создать индексы, заполнить данными
        es.fillElasticSearch();
    }

    @AfterAll
    static void afterAll() {
        es.finalizeElasticSearch();
        if (testServer != null) {
            testServer.stop();
        }
    }

    /**
     * Проверка десериализации ответов.
     * Ошибка может быть при десериализации самих данных (Order) или при десериализации служебных данных (dsl эластика).
     */
    @Nested
    @DisplayName("elastic answer deserialization test")
    class DeserializeESResponseTest {
        private <R> R deserializeHits(Class<R> clazz, ByteString response) {
            StrictMessageSerializer<R> messageSerializer =
                    (new ElasticSerializerFactory()).messageSerializerFor(clazz);
            MessageSerializer.NegotiatedDeserializer<R, ByteString> deserializer =
                    messageSerializer.deserializer(messageSerializer.acceptResponseProtocols().get(0));
            return deserializer.deserialize(response);
        }

        @Test
        @DisplayName("must deserialize ES answer with Hits<List<HitResult<Order>>>")
        void testDeserializeHits() {
            final String order = "{\"id\": \"14\"," +
                    "\"creationDate\": \"2019-01-17T13:00:08.507+03:00\"," +
                    "\"orderState\": \"state 665\"," +
                    "\"orderType\": \"type new\"," +
                    "\"region\": \"Москва\"," +
                    "\"city\": \"City 744\"," +
                    "\"salePointId\": \"#761\"," +
                    "\"salePointAddress\": \"address 343\"," +
                    "\"shipGroupType\": 469," +
                    "\"totalPrice\": 6.4," +
                    "\"payment\":  { \"type\": \"type 271\"," +
                    "\"state\": \"payment state new\"}," +
                    "\"comment\": \"Комментарий кириллическими буквами для заказа #148727568862787057712\"," +
                    "\"clientEmail\": \"client@company.z849\"," +
                    "\"clientPhone\": \"892\"," +
                    "\"orderComposition\": []," +
                    "\"promo\": \"407\"," +
                    "\"parentNetwork\": \"431\"," +
                    "\"network\": \"795\"," +
                    "\"cancelReason\": \"501\"}";
            final String hitResult = "{\"_index\": \"order\"," +
                    "\"_type\": \"order\"," +
                    "\"_id\": \"1\"," +
                    "\"_score\": 1," +
                    "\"_source\":" +
                    order +
                    "}";
            final String hits = "{" +
                    "   \"total\": 1," +
                    "   \"max_score\": 1," +
                    "   \"hits\":[" +
                    hitResult +
                    "]}";
            final String response = "{\"took\": 7," +
                    "\"timed_out\": false," +
                    "\"hits\":" +
                    hits +
                    "}";
            assertAll("Десериализация ответа ES на запрос поиска заказов.",
                () -> assertThat(deserializeHits(OrderSearchResult.class, ByteString.fromString(response)))
                        .withFailMessage(
                                String.format(S_ACCERT_CANT_DESERIALIZE_ES_SEARCH_RESPONSE, OrderSearchResult.class.getSimpleName()))
                        .isNotNull()
            );
        }
    }

    /** Test class.
     */
    @Nested
    @DisplayName("Find orders by its fields")
    class FindOrdersByFieldsTest {

        private FindOrdersResponse invokeFindOrdersViaElastic(FindOrdersRequest findOrdersRequest) {
            ElasticRepository repository = testServer.injector().instanceOf(ElasticRepository.class);
            return repository.findOrders(findOrdersRequest).toCompletableFuture().join();
        }

        private OrderCard invokeGetOrderViaElastic(Long id) {
            ElasticRepository repository = testServer.injector().instanceOf(ElasticRepository.class);
            return repository.getOrder(id).toCompletableFuture().join();
        }

        /**
         * Поиск заказа через elastic напрямую.
         * Поиск по ID, городу, региону, комментарию, клиенту, дате.
         */
        @Test
        @DisplayName("orders must be found by separate fields directly in ES")
        void testFindOrdersByFieldsViaElastic() {
            findOrdersByFields(this::invokeFindOrdersViaElastic);
        }

        /**
         * Поиск заказов указанным методом.
         */
        private void findOrdersByFields(Function<FindOrdersRequest, FindOrdersResponse> search) {
            final int dateSearchRangeWidth = 300;
            assertAll("Поиск заказа по ID, городу, региону, комментарию, клиенту, дате.",
                () -> assertThat(search.apply(FindOrdersRequest.builder().id("1").build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_ID_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ((Order)p).getId().startsWith("1")),
                () -> assertThat(search.apply(FindOrdersRequest.builder().id("-1").build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_FOUND_FAKE_BY_ID_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isEmpty(),
                () -> assertThat(search.apply(FindOrdersRequest.builder().siteId(TEST_ORDER_SITEID).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_SITEID_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ((Order)p).getSiteId().equals(TEST_ORDER_SITEID)),
                () -> assertThat(search.apply(FindOrdersRequest.builder().cityId(TEST_ORDER_CITY_ID).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_CITYID_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ((Order)p).getDelivery().getCityId().equals(TEST_ORDER_CITY_ID)),
                () -> assertThat(search.apply(FindOrdersRequest.builder().salePointId(TEST_ORDER_SALEPOINT_RUS).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_SALEPOINT_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ((Order)p).getDelivery().getSalePointId().equals(TEST_ORDER_SALEPOINT_RUS)),
                () -> assertThat(search.apply(FindOrdersRequest.builder().comment(TEST_ORDER_COMMENT).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_COMMENT_PART_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        // У лида и заказа разные комментарии
                        .anyMatch(p -> ((Order)p).getComment().startsWith(TEST_ORDER_COMMENT)),
                () -> assertThat(search.apply(FindOrdersRequest.builder().clientName(TEST_ORDER_CLIENT_PREF).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_CLIENT_PART_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ((Order)p).getClientName().startsWith(TEST_ORDER_CLIENT_PREF)),
                () -> assertThat(search.apply(FindOrdersRequest.builder()
                        .dateFrom(ZonedDateTime.now().minusDays(dateSearchRangeWidth)).dateTo(ZonedDateTime.now()).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_DATE_RANGE_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ZonedDateTime.now().isAfter(((Order)p).getCreationDate())),
                () -> assertThat(search.apply(FindOrdersRequest.builder().deliveryId(TEST_ORDER_DELIVERY_ID).build()))
                        .withFailMessage(String.format(S_ASSERT_RECORD_NOT_FOUND_BY_DELIVERY_ID_FMT, S_TYPE_ORDER))
                        .isNotNull()
                        .extracting(FindOrdersResponse::getOrders)
                        .asList()
                        .isNotNull()
                        .isNotEmpty()
                        .anyMatch(p -> ((Order)p).getDelivery().getId().startsWith(TEST_ORDER_COMMENT))
            );
        }

        /**
         * Поиск заказов с учетом сложных фильтров указанным способом.
         */
        private void findOrdersByFlags(Function<FindOrdersRequest, FindOrdersResponse> search,
                                       Function<Long, OrderCard> get) {
            final int pageSize = 100;
            assertAll("Поиск заказа по признакам заполненности и незаполненности полей mnpи promo.",
                // Проверки по наличию/отсутствию promo и mnp
                () -> assertThat(search.apply(FindOrdersRequest.builder().checkMnp(Boolean.TRUE).build())
                        .getOrders()
                        .stream()
                        .map(order -> get.apply(Long.parseLong(order.getId()))))
                        .withFailMessage(String.format(S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_TRUE, S_TYPE_ORDER))
                        .extracting(OrderCard::getMnp)
                        .isNotEmpty(),
                () -> assertThat(search.apply(FindOrdersRequest.builder().checkMnp(Boolean.FALSE).build())
                        .getOrders()
                        .stream()
                        .map(order -> get.apply(Long.parseLong(order.getId()))))
                        .withFailMessage(String.format(S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_FALSE, S_TYPE_ORDER))
                        .extracting(OrderCard::getMnp)
                        .isEmpty(),
                () -> assertThat(search.apply(FindOrdersRequest.builder().checkMnp(null).pageSize(pageSize).build())
                        .getOrders())
                        .withFailMessage(String.format(S_ASSERT_TOO_LESS_RECORD_FOUND_BY_MNP_VOID, S_TYPE_ORDER))
                        .isNotNull()
                        .hasSize(pageSize),
                () -> assertThat(search.apply(FindOrdersRequest.builder().checkPromo(Boolean.TRUE).build())
                        .getOrders()
                        .stream()
                        .map(order -> get.apply(Long.parseLong(order.getId()))))
                        .withFailMessage(String.format(S_ASSERT_TOO_LESS_RECORD_FOUND_BY_PROMO_TRUE, S_TYPE_ORDER))
                        .extracting(OrderCard::getPromo)
                        .isNotEmpty(),
                () -> assertThat(search.apply(FindOrdersRequest.builder().checkPromo(Boolean.FALSE).build())
                        .getOrders()
                        .stream()
                        .map(order -> get.apply(Long.parseLong(order.getId()))))
                        .withFailMessage(String.format(S_ASSERT_TOO_LESS_RECORD_FOUND_BY_PROMO_FALSE, S_TYPE_ORDER))
                        .extracting(OrderCard::getPromo)
                        .isEmpty(),
                () -> assertThat(search.apply(FindOrdersRequest.builder().checkPromo(null).pageSize(pageSize).build())
                        .getOrders())
                        .withFailMessage(String.format(S_ASSERT_TOO_LESS_RECORD_FOUND_BY_PROMO_VOID, S_TYPE_ORDER))
                        .isNotNull()
                        .hasSize(pageSize)
            );
        }

        /**
         * Поиск заказов с учетом сложных фильтров напрямую через ES.
         */
        @Test
        @DisplayName("orders must be found by flags \"mnp\" and \"promo\", by composition string directly in ES")
        void findOrdersByFlagsViaElastic() {
            findOrdersByFlags(this::invokeFindOrdersViaElastic, this::invokeGetOrderViaElastic);
        }

        /**
         * Поиск заказов по их составу.
         */
        private void findOrdersByComposition(Function<FindOrdersRequest, FindOrdersResponse> search,
                                             Function<Long, OrderCard> get) {
            String testComposition1 = String.format("aaa %s bbb", TEST_ORDER_FRONT_SUBSTR_TERM_1);
            String testComposition2 = String.format("aaa %s bbb", TEST_ORDER_FRONT_SUBSTR_TERM_2);
            String testComposition3 =
                    TEST_ORDER_SKU_TYPE_SEARCHABLE_PREFIX.substring(0, TEST_ORDER_SKU_TYPE_SEARCHABLE_PREFIX.length() / 2);
            // по данной подстроке сформировать запрос и получить список заказов
            Function<String, FindOrdersResponse> getOrders =
                composition -> search.apply(FindOrdersRequest.builder().orderComposition(composition).pageSize(GEN_ORDER_COUNT).build());
            assertAll("Поиск заказа по его составу.",
                () -> assertThat(getOrders.apply(TEST_ORDER_ITEM_STATE_PREFIX)
                        .getOrders())
                    .withFailMessage(S_ASSERT_ORDER_COMP_NOT_FOUND_BY_PREFIX)
                    .isNotNull()
                    .isNotEmpty(),
                // для каждого найденного заказа вернуть 1 если нет состава или в составе хоть раз встречается TEST_ORDER_FRONT,
                // просуммировать - должно получиться количество всех заказов
                () -> assertThat(getOrders.apply(testComposition1)
                        .getOrders()
                        .stream()
                        .map(order -> get.apply(Long.parseLong(order.getId())))
                        .map(OrderCard::getItems)
                        .flatMap(Collection::stream))
                        .withFailMessage(S_ASSERT_ORDER_COMP_NOT_FOUND_BY_WORD_FORM)
                        .anyMatch(line ->
                            line.getItem().getFrontName().equals(TEST_ORDER_FRONT) ||
                            line.getSubItem().getFrontName().equals(TEST_ORDER_FRONT)),
                // поиск по термину, заданному TEST_ORDER_FRONT_SUBSTR_TERM_2, термин должен быть во всех записях заказа, имеющих состав
                () -> assertThat(getOrders.apply(testComposition2)
                        .getOrders()
                        .stream()
                        .map(order -> get.apply(Long.parseLong(order.getId())))
                        .map(OrderCard::getItems)
                        .flatMap(Collection::stream))
                        .withFailMessage(S_ASSERT_ORDER_COMP_NOT_FOUND_BY_WORD_FORMS)
                        .anyMatch(line ->
                            line.getItem().getFrontName().contains(TEST_ORDER_FRONT_SUBSTR_TERM_2) ||
                            line.getSubItem().getFrontName().contains(TEST_ORDER_FRONT_SUBSTR_TERM_2)),
                // поиск по части строки из состава заказа
                () -> assertThat(search.apply(FindOrdersRequest.builder().orderComposition(testComposition3).build())
                        .getOrders())
                        .withFailMessage(S_ASSERT_ORDER_COMP_NOT_FOUND_PHRASE_PREFIX)
                        .isNotNull()
                        .isNotEmpty()
            );
        }

        /**
         * Поиск заказов с учетом сложных фильтров.
         */
        @Test
        @DisplayName("orders must be found by composition string directly in ES")
        void findOrdersByCompositionViaElastic() {
            findOrdersByComposition(this::invokeFindOrdersViaElastic, this::invokeGetOrderViaElastic);
        }

    }

    /**
     * Заглушка для тестирования в отсутствии реально работающего внешнего сервиса.
     *
     * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
     */
    static class OrderEventsStub implements Service, OrderEventsClient {

        @Inject
        OrderEventsStub(ProducerStubFactory producerFactory) {
            orderProducer = producerFactory.producer(OrderEvents.TOPIC_ORDER);
        }

        @Override
        public Topic<OrderEvent> getTopicOrder() {
            return orderProducer.topic();
        }

        @Override
        public Descriptor descriptor() {
            return named("order-topic")
                    .withTopics(topic(OrderEvents.TOPIC_ORDER, this::getTopicOrder))
                    .withAutoAcl(true);
        }

    }

    /**
     * Репозиторий с логированием, для тестирования.
     *
     * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
     */
    @Singleton
    static class ElasticRepositoryStub extends ElasticRepository {

        @Inject
        ElasticRepositoryStub(ElasticDocument elasticDocument, ElasticSearch elasticSearch) {
            super(elasticDocument, elasticSearch);
        }

        @Override
        public CompletionStage<FindOrdersResponse> findOrders(FindOrdersRequest findOrdersRequest) {
            try {
                LOGGER.info(String.format("request json: [FILTER]%s[/FILTER]",
                        ElasticSerializerFactory.getMAPPER().writeValueAsString(findOrdersRequest)));
            } catch (JsonProcessingException e) {
                LOGGER.info(String.format("filter json: <error> %s", e.getMessage()));
            }
            SearchRequest searchRequest = getFindOrdersRequest(findOrdersRequest);
            try {
                LOGGER.info(String.format("request json: [REQUEST]%s[/REQUEST]",
                        ElasticSerializerFactory.getMAPPER().writeValueAsString(searchRequest)));
            } catch (JsonProcessingException e) {
                LOGGER.info(String.format("request json: <error> %s", e.getMessage()));
            }
            return findOrdersByRequest(searchRequest);
        }

        @Override
        public CompletionStage<OrderCard> getOrder(Long id) {
            return ServiceCall.invoke(
                    es.getElasticDocument().getSource(ORDER_INDEX, ORDER_TYPE, id.toString()),
                    NotUsed.getInstance(), OrderCardSearchResult.class)
                    .thenApply(result -> ofNullable(result)
                            .map(OrderCardSearchResult::getHits)
                            .map(Hits::getHits)
                            .map(Collection::stream).orElse(Stream.empty())
                            .map(HitResult::getSource)
                            .map(Converters.ESToApi::toOrderCard)
                            .peek(order -> LOGGER.info(String.format("Get(%d)=%s", id, order)))
                            .findFirst()
                            .orElseThrow(() -> new NotFound("Order card not found!"))
                    );
        }

    }

}
