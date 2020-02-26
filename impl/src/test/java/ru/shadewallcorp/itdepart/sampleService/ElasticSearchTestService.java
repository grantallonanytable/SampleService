package ru.shadewallcorp.itdepart.sampleService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbend.lagom.javadsl.client.integration.LagomClientFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taymyr.lagom.elasticsearch.ServiceCall;
import org.taymyr.lagom.elasticsearch.document.ElasticDocument;
import org.taymyr.lagom.elasticsearch.document.dsl.update.DocUpdateRequest;
import org.taymyr.lagom.elasticsearch.indices.ElasticIndices;
import org.taymyr.lagom.elasticsearch.indices.dsl.CreateIndex;
import org.taymyr.lagom.elasticsearch.indices.dsl.DataType;
import org.taymyr.lagom.elasticsearch.indices.dsl.Mapping;
import org.taymyr.lagom.elasticsearch.indices.dsl.MappingProperty;
import org.taymyr.lagom.elasticsearch.search.ElasticSearch;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;
import ru.shadewallcorp.itdepart.sampleService.elastic.Address;
import ru.shadewallcorp.itdepart.sampleService.elastic.CatalogId;
import ru.shadewallcorp.itdepart.sampleService.elastic.Client;
import ru.shadewallcorp.itdepart.sampleService.elastic.Delivery;
import ru.shadewallcorp.itdepart.sampleService.elastic.ElasticRepository;
import ru.shadewallcorp.itdepart.sampleService.elastic.IdentityCard;
import ru.shadewallcorp.itdepart.sampleService.elastic.Money;
import ru.shadewallcorp.itdepart.sampleService.elastic.Order;
import ru.shadewallcorp.itdepart.sampleService.elastic.OrderCompositionItem;
import ru.shadewallcorp.itdepart.sampleService.elastic.Payment;
import ru.shadewallcorp.itdepart.sampleService.elastic.SalePoint;
import ru.shadewallcorp.itdepart.sampleService.elastic.SkuItem;
import ru.shadewallcorp.itdepart.sampleService.elastic.SkuSubItem;
import ru.shadewallcorp.itdepart.sampleService.elastic.TariffZone;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;
import static java.math.RoundingMode.HALF_UP;
import static org.taymyr.lagom.elasticsearch.indices.dsl.DynamicType.FALSE;
import static org.taymyr.lagom.elasticsearch.indices.dsl.DynamicType.TRUE;

/**
 * Методы работы с ES, подготовка эластика для автотестов.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
final class ElasticSearchTestService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchTest.class);

    // Сколько заказов сгенерировать
    public static final int GEN_ORDER_COUNT = 100;
    public static final String TEST_ORDER_CLIENT_PREF = "client # ";
    public static final String TEST_ORDER_COMMENT = "Комментарий кириллическими буквами для заказа";
    public static final String TEST_ORDER_COMMENT_FMT = TEST_ORDER_COMMENT + " #%d";
    public static final String TEST_ORDER_ITEM_STATE = "Без статуса";
    public static final String TEST_ORDER_ITEM_STATE_PREFIX = "Статус_";
    public static final String TEST_ORDER_FRONT = "тестовое название продукта из данного каталога";
    public static final String TEST_ORDER_FRONT_PREFIX = "продукт с названием FRONT_";
    public static final String TEST_ORDER_SITEID = "msk";
    public static final String TEST_ORDER_SALEPOINT_RUS = "Точка 1";
    public static final String TEST_ORDER_FRONT_SUBSTR_TERM_1 = "данный";
    public static final String TEST_ORDER_FRONT_SUBSTR_TERM_2 = "продукт";
    public static final String TEST_ORDER_SKU_TYPE_SEARCHABLE_PREFIX = "Тестовый";
    public static final String TEST_ORDER_SKU_TYPE = TEST_ORDER_SKU_TYPE_SEARCHABLE_PREFIX + " СКУ";

    public static final String TEST_ORDER_CATALOG_FILEDS_PREFIX = "- item";
    public static final String TEST_ORDER_CITY = "City ";
    public static final String TEST_ORDER_CITY_ID = "12345";
    public static final String TEST_ORDER_COMM_ID = "Новый comm_id";
    public static final String TEST_ORDER_SITEID_RANDOM_PREF = "region_";
    public static final String TEST_ORDER_PRODUCT_ID_PREFIX = "PRODUCT";
    public static final String TEST_ORDER_PRODUCT_ID = TEST_ORDER_PRODUCT_ID_PREFIX + " DEFAULT_000000";
    public static final String TEST_ORDER_SKU_ID = "SKU00000000";
    public static final String TEST_ORDER_SKU_SUB_ITEM_TYPE = "sku_sub_type";
    public static final String TEST_ORDER_DELIVERY_ID = "100000";

    public static final Integer EMBEDDED_ELASTIC_HTTP_PORT = 9350;
    public static final String EMBEDDED_ELASTIC_HTTP = "http://localhost:" + EMBEDDED_ELASTIC_HTTP_PORT;
    // Индекс для вложенных полей состава заказа
    public static final String ORDER_COMPOSITION_FIELD = "orderComposition";
    public static final String ORDER_COMPOSITION_AGGREGATED_FIELD = "orderCompositionAggregated";

    private EmbeddedElastic embeddedElastic;
    @Getter
    private ElasticDocument elasticDocument;
    @Getter
    private ElasticSearch elasticSearch;
    @Getter
    private ElasticIndices elasticIndices;
    private LagomClientFactory clientFactoryES;
    private boolean isStarted;

    /**
     * Создать интерфейсы служб ES.
     */
    ElasticSearchTestService() throws URISyntaxException {
        clientFactoryES = LagomClientFactory.create("elasticsearch", LagomClientFactory.class.getClassLoader());
        elasticDocument = clientFactoryES.createClient(ElasticDocument.class, new URI(EMBEDDED_ELASTIC_HTTP));
        elasticSearch = clientFactoryES.createClient(ElasticSearch.class, new URI(EMBEDDED_ELASTIC_HTTP));
        elasticIndices = clientFactoryES.createClient(ElasticIndices.class, new URI(EMBEDDED_ELASTIC_HTTP));
    }

    void finalizeElasticSearch() {
        if (clientFactoryES != null) {
            clientFactoryES.close();
        }
        if (embeddedElastic != null) {
            embeddedElastic.stop();
        }
    }

    void startElasticSearchServer()
            throws IOException, InterruptedException {
        if (!isStarted) {
            embeddedElastic = EmbeddedElastic.builder()
                    .withElasticVersion("6.4.1")
                    .withSetting(PopularProperties.HTTP_PORT, EMBEDDED_ELASTIC_HTTP_PORT)
                    // This setting prevents indices to be turned to the 'read-only' state
                    .withSetting("cluster.routing.allocation.disk.threshold_enabled", false)
                    .withStartTimeout(5, TimeUnit.MINUTES)
                    .withCleanInstallationDirectoryOnStop(false)
                    .build()
                    .start();
            isStarted = true;
        }
    }

    /**
     * Заполнение тестовыми данными.
     */
    void fillElasticSearch() {
        // создать индексы по полям фильтра для поиска заказов
        createIndex(ElasticRepository.ORDER_INDEX, ElasticSearchTestService::getElasticOrderIndexMapProperties, false);
        // заполнить репозиторий
        orderEventConsumerTestGenerateData();
    }

    /**
     * Генерировать свойства для структурного поля по его полям.
     *
     * @param fields - список полей вложенного объекта (любое может быть вложенным объектом)
     * @return свойство поля в ES
     */
    private static MappingProperty simpleObjectFieldType(Map<String, MappingProperty> fields) {
        return MappingProperty.builder()
                .type(DataType.OBJECT)
                .properties(fields).build();
    }

    /**
     * Генерировать свойства для структурного поля по его полям.
     *
     * @param fields - список полей вложенного объекта (любое может быть вложенным объектом)
     * @return свойство поля в ES
     */
    private static MappingProperty nestedObjectFieldType(Map<String, MappingProperty> fields) {
        return MappingProperty.builder()
                .type(DataType.NESTED)
                .properties(fields).build();
    }

    /**
     * Создать свойство для текстового поля c анализатором семантики русского языка (для поддержки полнотекстового поиска).
     *
     * @return свойство поля для добавления в индекс.
     */
    private static MappingProperty mappingPropertyTextRus() {
        return MappingProperty.builder().type(DataType.TEXT).analyzer("russian").build();
    }

    /**
     * Создать свойство для текстового поля строки состава заказа с учетом агрегирования данных в специальное поле заказа.
     *
     * @param dataType тип поля данных.
     * @return свойство поля для добавления в индекс.
     */
    private static MappingProperty mappingPropertyAggr(@NotNull DataType dataType) {
        return MappingProperty.builder().type(dataType).copyTo(ORDER_COMPOSITION_AGGREGATED_FIELD).build();
    }

    /**
     * Получить карту полей элементов состава заказа для регистрации индекса в elastic.
     */
    private static Map<String, MappingProperty> getOrderCompositionIndexMapProperties() {
        // состав заказа - составное поле, необходимо собрать меппинги для него
        Map<String, MappingProperty> priceFields = new HashMap<>();
        priceFields.put("amount", MappingProperty.DOUBLE);
        priceFields.put("currency", mappingPropertyAggr(DataType.KEYWORD));
        Map<String, MappingProperty> salePriceFields = new HashMap<>(priceFields);
        Map<String, MappingProperty> catalogIdFields = new HashMap<>();
        catalogIdFields.put("productId", mappingPropertyAggr(DataType.KEYWORD));
        catalogIdFields.put("skuId", mappingPropertyAggr(DataType.KEYWORD));
        catalogIdFields.put("serialId", mappingPropertyAggr(DataType.KEYWORD));
        Map<String, MappingProperty> skuSubItemFields = new HashMap<>();
        skuSubItemFields.put("commerceId", mappingPropertyAggr(DataType.KEYWORD));
        skuSubItemFields.put("catalogId", simpleObjectFieldType(catalogIdFields));
        skuSubItemFields.put("type", mappingPropertyAggr(DataType.KEYWORD));
        skuSubItemFields.put("frontName", mappingPropertyAggr(DataType.TEXT));
        skuSubItemFields.put("mnp", mappingPropertyAggr(DataType.KEYWORD));
        skuSubItemFields.put("amount", MappingProperty.INTEGER);
        skuSubItemFields.put("price", simpleObjectFieldType(priceFields));
        Map<String, MappingProperty> skuItemFields = new HashMap<>(skuSubItemFields);
        skuItemFields.put("salePrice", simpleObjectFieldType(salePriceFields));
        // В ES нет типа "массив", каждое поле может иметь одно и более значений одного типа
        Map<String, MappingProperty> orderCompositionFields = new HashMap<>();
        orderCompositionFields.put("item", simpleObjectFieldType(skuItemFields));
        orderCompositionFields.put("subItem", simpleObjectFieldType(skuSubItemFields));
        orderCompositionFields.put("itemState", mappingPropertyAggr(DataType.KEYWORD));
        return orderCompositionFields;
    }

    /**
     * Получить карту полей фильтра для регистрации индекса в elastic.
     */
    private static Map<String, MappingProperty> getElasticOrderIndexMapProperties() {
        // TODO переделать индекс
        Map<String, MappingProperty> res = new HashMap<>();
        res.put("id", MappingProperty.TEXT);
        res.put("creationDate", MappingProperty.DATE);
        res.put("orderState", MappingProperty.KEYWORD);
        res.put("orderType", MappingProperty.KEYWORD);
        res.put("region", MappingProperty.KEYWORD);
        res.put("city", MappingProperty.KEYWORD);
        res.put("salePointId", MappingProperty.KEYWORD);
        res.put("salePointAddress", mappingPropertyTextRus());
        res.put("shipGroupType", MappingProperty.INTEGER);
        res.put("clientName", mappingPropertyTextRus());
        res.put("clientEmail", mappingPropertyTextRus());
        res.put("clientPhone", mappingPropertyTextRus());
        res.put("totalPrice", MappingProperty.DOUBLE);
        res.put("paymentType", MappingProperty.KEYWORD);
        res.put("paymentState", MappingProperty.KEYWORD);
        res.put("trackNumber", mappingPropertyTextRus());
        res.put("comment", mappingPropertyTextRus());
        res.put("mnp", mappingPropertyTextRus());
        res.put("promo", MappingProperty.KEYWORD);
        res.put("parentNetwork", mappingPropertyTextRus());
        res.put("network", mappingPropertyTextRus());
        res.put("partner", mappingPropertyTextRus());
        res.put("cancelReason", MappingProperty.KEYWORD);
        res.put(ORDER_COMPOSITION_AGGREGATED_FIELD, mappingPropertyTextRus());
        res.put(ORDER_COMPOSITION_FIELD, simpleObjectFieldType(getOrderCompositionIndexMapProperties()));
        // Страницы поиска - отдельные параметры поиска, не для индекса
        return res;
    }

    /**
     * Создать индекс по полям фильтра для поиска заказов.
     */
    private void createIndex(String indexName, Supplier<Map<String, MappingProperty>> getIndexMapProperties,
                             boolean canModifyIndex) {
        Map<String, Mapping> elasticIndexMap = new HashMap<>();
        elasticIndexMap.put(indexName, new Mapping(getIndexMapProperties.get(), canModifyIndex ? TRUE : FALSE));
        CreateIndex elasticIndex = new CreateIndex(
                new CreateIndex.Settings(5, 1, null),
                elasticIndexMap);
        try {
            LOGGER.info(String.format("[BeforeALL] Index json=[%s]", (new ObjectMapper()).writeValueAsString(elasticIndex)));
        } catch (JsonProcessingException e) {
            LOGGER.info(String.format("[BeforeALL] Index creating: error while forming json: %s", e.getMessage()));
        }
        elasticIndices.create(indexName)
                .invoke(elasticIndex)
                .exceptionally(error -> {
                            LOGGER.info(String.format("[BeforeALL] Index not created: err=%s", error.getMessage()));
                            return null;
                        }
                )
                .thenApply(createIndexResult -> {
                    LOGGER.info(String.format("[BeforeALL] Index created: %s", createIndexResult.component3()));
                    embeddedElastic.refreshIndices();
                    return createIndexResult;
                })
                .toCompletableFuture();
    }

    /**
     * Вернуть непустое значение или значение по умолчанию.
     */
    private static <T> T nvl(T value, T defValue) {
        return value != null ? value : defValue;
    }

    /**
     * Вернуть непустое значение или рандомно: случайное значение или фиксированное значение по умолчанию.
     */
    private static String nvlRandom(String value, String defValuePref, String defValueStatic, Random random) {
        // Магические числа для генерации разномастных данных
        final int randomSomeObjId = 1000;
        return value != null ? value : random.nextInt(10) < 7 ? defValuePref + random.nextInt(randomSomeObjId) : defValueStatic;
    }

    /**
     * Вернуть непустое значение или рандомно: случайное значение или null.
     */
    private static String nvlRandomMnp(String value, Random random) {
        // Магические числа для генерации разрядов номера телефона
        return value != null ? value : random.nextInt(10) < 3 ? null :
                String.format("8-977-%03d-%02d-%02d", random.nextInt(1000), random.nextInt(100), random.nextInt(100));
    }

    /**
     * Генерировать вложенное поле для заказа.
     */
    private static Money generateOrderFieldCurrencyAmount(Random random, int maxRandomVal) {
        return Money.builder()
                .amount(BigDecimal.valueOf(random.nextInt(maxRandomVal) / 100.0))
                .currency("RUB")
                .build();
    }

    /**
     * Генерировать вложенное поле для заказа.
     */
    private static CatalogId generateOrderFieldCatalogId(Random random, @NotNull String valuesPrefix) {
        return CatalogId.builder()
                .productId(nvlRandom(null, TEST_ORDER_PRODUCT_ID_PREFIX, TEST_ORDER_PRODUCT_ID, random) + valuesPrefix)
                .skuId(nvlRandom(null, "", TEST_ORDER_SKU_ID, random) + valuesPrefix)
                .serialId(nvlRandom(null, "", null, random) + valuesPrefix)
                .build();
    }

    private static SkuSubItem generateOrderFieldSkuSubItem(Random random, int maxRandomVal) {
        // catalogId есть не у всех записей - для проверки поиска по каталогу
        CatalogId catalogId = null;
        if (random.nextInt(10) < 7) {
            catalogId = generateOrderFieldCatalogId(random, "");
        }
        return SkuSubItem.builder()
                .commerceId(nvlRandom(null, "SUB_COMM_ID_", null, random))
                .type(nvlRandom(null, "SUB_TYPE_", TEST_ORDER_SKU_SUB_ITEM_TYPE, random))
                .frontName(nvlRandom(null, "SUB_FRONT_", null, random))
                .catalogId(catalogId)
                .amount(random.nextInt(maxRandomVal))
                .build();
    }

    private static SkuItem generateOrderFieldSkuItem(Random random, int maxRandomVal) {
        // catalogId есть не у всех записей - для проверки поиска по каталогу
        CatalogId catalogId = null;
        if (random.nextInt(10) < 7) {
            catalogId = generateOrderFieldCatalogId(random, TEST_ORDER_CATALOG_FILEDS_PREFIX);
        }
        return SkuItem.builder()
                .commerceId(nvlRandom(null, "COMM_ID_", TEST_ORDER_COMM_ID, random))
                .type(nvlRandom(null, "TYPE_", TEST_ORDER_SKU_TYPE, random))
                .frontName(nvlRandom(null, TEST_ORDER_FRONT_PREFIX, TEST_ORDER_FRONT, random))
                .mnp(nvlRandom(null, "", null, random))
                .catalogId(catalogId)
                .amount(random.nextInt(maxRandomVal))
                .price(generateOrderFieldCurrencyAmount(random, maxRandomVal))
                .salePrice(generateOrderFieldCurrencyAmount(random, maxRandomVal / 10))
                .build();
    }

    @NotNull
    private static List<OrderCompositionItem> getOrderFieldsComposition(Random random, int maxRandomVal) {
        List<OrderCompositionItem> res = new ArrayList<>();
        // У 20% заказов будет состав, у 4% состав из двух элементов.
        while (res.isEmpty() || random.nextInt(10) < 2) {
            res.add(OrderCompositionItem.builder()
                    .item(generateOrderFieldSkuItem(random, maxRandomVal))
                    .subItem(generateOrderFieldSkuSubItem(random, maxRandomVal))
                    .itemState(nvlRandom(null, TEST_ORDER_ITEM_STATE_PREFIX, TEST_ORDER_ITEM_STATE, random))
                    .build());
            if (res.size() > 2) {
                LOGGER.info(String.format("Count of items in composition: %d", res.size()));
            }
        }
        return res;
    }

    @NotNull
    private static Client generateClient(Client source, Random random) {
        return Client.builder()
                .gender("male")
                .name(nvlRandom(source.getName(), TEST_ORDER_CLIENT_PREF, null, random))
                .email(nvlRandom(source.getEmail(), "client", null, random) + "@company.com")
                .phone(nvlRandom(source.getPhone(), "", null, random))
                .identityCard(IdentityCard.builder()
                        .series(String.format("%04d", random.nextInt(1000)))
                        .number(String.format("%06d", random.nextInt(1000)))
                        .issuedBy("issued by")
                        .issuedOn(ZonedDateTime.now().minusDays(random.nextInt(100)).toLocalDate())
                        .issuedCode("770-020")
                        .birthday(ZonedDateTime.now().minusDays(random.nextInt(1000)).toLocalDate())
                        .placeOfBirth("place of birth")
                        .build())
                .address(generateAddress(random))
                .build();
    }

    @NotNull
    private static Delivery generateDelivery(Delivery source, Random random) {
        final int maxRandomVal = 1000;
        return Delivery.builder()
                .id(TEST_ORDER_DELIVERY_ID)
                .alias("alias")
                .name("delivery name")
                .cityId(nvlRandom(source.getCityId(), null, TEST_ORDER_CITY_ID, random))
                .shipGroupType(nvl(source.getShipGroupType(), random.nextInt(maxRandomVal)))
                .trackNumber(nvlRandom(source.getTrackNumber(), "RM", null, random))
                .courierInfo(generateAddress(random))
                .salePoint(new SalePoint(nvlRandom(source.getSalePoint().getId(), "#", TEST_ORDER_SALEPOINT_RUS, random),
                        nvlRandom(source.getSalePoint().getAddress(), "address ", null, random)))
                .tariffZone(TariffZone.builder()
                        .cost(new Money(BigDecimal.ONE, "RUR"))
                        .name("tariff zone name")
                        .deliveryTimeDesc("tariff zone desc")
                        .build())
                .build();
    }

    @NotNull
    private static Address generateAddress(Random random) {
        return Address.builder()
                .city(TEST_ORDER_CITY)
                .postalCode("127100")
                .apartment(String.valueOf(random.nextLong()))
                .building(String.valueOf(random.nextLong()))
                .street("street")
                .build();
    }

    /**
     * Генерация заказа со случайными данными на основе шаблона, в котором можно указать необходимые поля (напр., ID).
     *
     * @param source шаблон с необходимыми заполненными полями
     * @return запись со случайными данными, некоторые поля могут не быть заполненными.
     */
    private static Order fillRandomOrder(Order source) {
        Random random = new Random(DateTime.now().getMillis() * Long.parseLong(source.getId()));
        // Магические числа для генерации разномастных данных
        final int maxRandomVal = 1000;
        final int randomDayFromShift = 200;
        List<OrderCompositionItem> items = source.getItems();
        if (items == null || items.isEmpty()) {
            items = getOrderFieldsComposition(random, maxRandomVal);
        }
        return Order.builder()
                .id(source.getId())
                .creationDate(ZonedDateTime.now().minusDays(random.nextInt(randomDayFromShift)).minusMinutes(random.nextInt(maxRandomVal)))
                .state(nvlRandom(source.getState(), "state ", "state new", random))
                .type(nvlRandom(source.getType(), "type ", "type new", random))
                .siteId(nvlRandom(source.getSiteId(), TEST_ORDER_SITEID_RANDOM_PREF, TEST_ORDER_SITEID, random))
                .totalPrice(new Money(
                        new BigDecimal(random.nextInt(maxRandomVal) / 100.0, new MathContext(2, HALF_UP)), "RUB"))
                .payment(new Payment(nvlRandom(source.getPayment().getType(), "type ", "payment type new", random),
                        nvlRandom(source.getPayment().getState(), "payment state ", "payment state new", random)))
                .comment(source.getComment() + random.nextLong())
                .mnp(nvlRandomMnp(source.getMnp(), random))
                .items(items)
                .promo(nvlRandom(source.getPromo(), "", null, random))
                .parentNetwork(nvlRandom(source.getParentNetwork(), "", null, random))
                .network(nvlRandom(source.getNetwork(), "", null, random))
                .partner(nvlRandom(source.getPartner(), "", null, random))
                .cancelReason(nvlRandom(source.getCancelReason(), "", null, random))
                .delivery(generateDelivery(source.getDelivery(), random))
                .client(generateClient(source.getClient(), random))
                .build();
    }

    /**
     * Сгенерировать тестовые данные - заказы.
     */
    private void orderEventConsumerTestGenerateData() {
        final int pauseMSAfterSend = 100;
        // Сгенерировать несколько (maxId) заказов с последовательными идентификаторами
        for (Integer i = 1; i <= GEN_ORDER_COUNT; i++) {
            insertOrderDirect(fillRandomOrder(
                    Order.builder().id(i.toString()).comment(String.format(TEST_ORDER_COMMENT_FMT, i)).build()));
            LOGGER.info(String.format("[TEST#orderEventConsumer] ... sent order id=%s", i));
            try {
                // Для перестройки индекса эластику нужно время.
                sleep(pauseMSAfterSend);
            } catch (InterruptedException e) {
                LOGGER.info(String.format("[TEST#orderEventConsumer] ... error while sending order id=%s", i));
            }
        }
    }

    private void insertOrderDirect(Order order) {
        DocUpdateRequest request =
                DocUpdateRequest.builder().doc(order).docAsUpsert(true).build();
        ServiceCall.invoke(elasticDocument.update(ElasticRepository.ORDER_INDEX, ElasticRepository.ORDER_TYPE, order.getId()), request)
                .thenAccept(updateResult -> LOGGER.info(String.format("Order inserted to ES: %s", updateResult)));
    }

}
