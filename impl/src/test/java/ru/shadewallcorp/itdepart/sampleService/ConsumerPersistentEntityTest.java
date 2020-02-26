package ru.shadewallcorp.itdepart.sampleService;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.ProducerStub;
import com.lightbend.lagom.javadsl.testkit.ProducerStubFactory;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.shadewallcorp.itdepart.sampleService.application.OrderEventConsumer;
import ru.shadewallcorp.itdepart.sampleService.application.OrderEventsClient;
import ru.shadewallcorp.itdepart.sampleService.elastic.ElasticRepository;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.Converters;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntity;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityCommand;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityEvent;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityState;
import ru.shadewallcorp.itdepart.sampleService.topic.InitOrder;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderCompositionItem;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderEvent;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderEvents;
import ru.shadewallcorp.itdepart.sampleService.topic.SkuItem;
import ru.shadewallcorp.itdepart.sampleService.topic.SkuSubItem;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.eventually;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Тестирование сервиса заказов.
 * Тесты запускать отдельно (кнопка слева от кода class ConsumerPersistentEntityTest).
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@DisplayName("Test Order Service - Kafka consumer and persistence Order Entity")
@Disabled
class ConsumerPersistentEntityTest {

    private static final String S_ASSERT_PRODUCED_ORDER_NOT_CONSUMED = "Не получен отправленный в Kafka заказ.";
    private static final String S_ASSERT_PERSISTENT_ENTITY = "Ошибка работы персистентной сущности";
    private static final String TEST_ORDER_ITEM_STATE = "Без статуса";

    private static ServiceTest.TestServer testServer;

    @BeforeAll
    static void beforeAll() {
        ServiceTest.Setup setup = defaultSetup().configureBuilder(
            builder -> builder.overrides(
                    bind(OrderEventConsumer.class).to(OrderEventConsumerStub.class),
                    bind(OrderEventsClient.class).to(OrderEventsStub.class)
            )
                    .configure("cassandra-query-journal.eventual-consistency-delay", "0")
        )
                .withCassandra()
                .withJdbc();
        testServer = ServiceTest.startServer(setup);
        // Тесты запускать по отдельности, если падает при запуске testServer
    }

    @AfterAll
    static void afterAll() {
        if (testServer != null) {
            testServer.stop();
        }
    }

    /**
     * Сгенерировать заказ.
     * @param orderId предопределённый id заказа
     * @return карточка заказа
     */
    private InitOrder generateOrder(@NotNull Long orderId) {
        PSequence<OrderCompositionItem> orderComposition = TreePVector.empty();
        orderComposition.plus(OrderCompositionItem.builder()
                .item(SkuItem.builder()
                        .type("skuItemType")
                        .build())
                .subItem(SkuSubItem.builder()
                        .type("skuSubItemType")
                        .build())
                .build());
        return InitOrder.builder().id(orderId.toString()).state(TEST_ORDER_ITEM_STATE).items(orderComposition).build();
    }

    /** Test class.
     */
    @Nested
    @DisplayName("Produce Kafka topics")
    class ProduceKafkaTopicsTest {

        private OrderEventsStub kafka;

        @BeforeEach
        void beforeEach() {
            // подготовить тестовый продьюсер
            kafka = testServer.injector().instanceOf(OrderEventsStub.class);
            OrderEventConsumerStub.orderReceivedList.clear();
        }

        /**
         * Генерация произвольного заказа и его запись в Kafka.
         * Заглушка собирает принятые заказы в orderReceivedList.
         */
        @Test
        @DisplayName("produce and consume random order")
        void testProduceKafkaTopics() {
            final Long orderId = 1000001L;
            kafka.sendOrder(generateOrder(orderId));
            eventually(Duration.create(5, SECONDS),
                () -> assertThat(OrderEventConsumerStub.orderReceivedList)
                        .withFailMessage(S_ASSERT_PRODUCED_ORDER_NOT_CONSUMED)
                        .extracting(
                                InitOrder::getId,
                                InitOrder::getState
                        )
                        .containsExactly(
                                tuple(
                                        orderId.toString(),
                                        TEST_ORDER_ITEM_STATE
                                )
                        )
            );
        }
    }

    /** Test class.
     */
    @Nested
    @DisplayName("Use persistent entity")
    class PersistentEntityTest {

        // Для тестирования персистентных сущностей
        private ActorSystem actorSystem;

        @BeforeEach
        void beforeEach() {
            // Для тестирования персистентной сущности
            actorSystem = ActorSystem.create();
        }

        @AfterEach
        void afterEach() {
            TestKit.shutdownActorSystem(actorSystem);
            actorSystem = null;
        }

        /**
         * Генерация произвольного заказа и его запись в Kafka.
         * Заглушка собирает принятые заказы в orderReceivedList.
         */
        @Test
        @DisplayName("add order event")
        void testAddRandomOrder() {
            final Long orderId = 1000002L;
            PersistentEntityTestDriver<OrderEntityCommand, OrderEntityEvent, OrderEntityState> driver =
                    new PersistentEntityTestDriver<>(actorSystem, new OrderEntity(), orderId.toString());

            InitOrder order = generateOrder(orderId);
            PersistentEntityTestDriver.Outcome<OrderEntityEvent, OrderEntityState> outcome =
                    driver.run(
                            new OrderEntityCommand.InsertOrder(Converters.KafkaToCassandra.toOrder(order)));
            assertThat(outcome)
                    .withFailMessage(S_ASSERT_PERSISTENT_ENTITY + "(1)")
                    .extracting(outcome1 -> outcome1.events().size())
                    .isEqualTo(1);
            assertThat(outcome)
                    .withFailMessage(S_ASSERT_PERSISTENT_ENTITY + "(2)")
                    .extracting(outcome1 -> outcome1.events().get(0))
                    .isEqualTo(new OrderEntityEvent.OrderEntityInserted(Converters.KafkaToCassandra.toOrder(order)));
            assertThat(outcome)
                    .withFailMessage(S_ASSERT_PERSISTENT_ENTITY + "(3)")
                    .extracting(outcome1 -> outcome1.state().isEmpty())
                    .isEqualTo(false);
            assertThat(outcome)
                    .withFailMessage(S_ASSERT_PERSISTENT_ENTITY + "(4)")
                    .extracting(outcome1 -> outcome1.state().getOrder())
                    .isEqualTo(order);
            assertThat(outcome)
                    .withFailMessage(S_ASSERT_PERSISTENT_ENTITY + "(5)")
                    .extracting(outcome1 -> outcome1.getReplies().get(0))
                    .isEqualTo(new OrderEntityCommand.InsertOrderDone(orderId.toString(), Converters.KafkaToCassandra.toOrder(order)));
            assertThat(outcome)
                    .withFailMessage(S_ASSERT_PERSISTENT_ENTITY + "(6)")
                    .extracting(PersistentEntityTestDriver.Outcome::issues)
                    .isEqualTo(emptyList());
        }
    }

    /**
     * Клиент кафки для тестирования (с логированием).
     */
    @Singleton
    static class OrderEventConsumerStub extends OrderEventConsumer {

        static List<InitOrder> orderReceivedList;

        @Inject
        OrderEventConsumerStub(PersistentEntityRegistry registry, ReadSide readSide,
                               OrderEventsClient orderEventsClient, ElasticRepository elasticRepository) {
            super(registry, readSide, orderEventsClient, elasticRepository);
            orderReceivedList = new ArrayList<>();
        }

        @Override
        protected void register(PersistentEntityRegistry registry, ReadSide readSide) {
            // void
        }

    }

    /**
     * Заглушка продюсера для тестирования.
     * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
     */
    @Singleton
    static final class OrderEventsStub implements OrderEventsClient {

        private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventsStub.class);
        private static ProducerStub<OrderEvent> orderProducerStub;

        /**
         * Создать продьюсер.
         */
        @Inject
        OrderEventsStub(ProducerStubFactory producerFactory) {
            orderProducerStub = producerFactory.producer(OrderEvents.TOPIC_ORDER);
        }

        @Override
        public Topic<OrderEvent> getTopicOrder() {
            return orderProducerStub.topic();
        }

        /**
         * Отправить в продьюсер Kafka.
         *
         * @param order заказ
         */
        void sendOrder(@NotNull InitOrder order) {
            CompletableFuture.runAsync(() -> {
                orderProducerStub.send(order);
                LOGGER.info(String.format("InitOrder sent: %s", order));
            });
        }
    }

}
