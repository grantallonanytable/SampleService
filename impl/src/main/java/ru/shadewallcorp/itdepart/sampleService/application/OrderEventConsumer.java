package ru.shadewallcorp.itdepart.sampleService.application;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import ru.shadewallcorp.itdepart.sampleService.elastic.ElasticRepository;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.Converters;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.Order;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntity;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityCommand.InsertOrder;
import ru.shadewallcorp.itdepart.sampleService.topic.InitOrder;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderEvent;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

import static akka.Done.done;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Хранилище persistence.
 * Предназначено для записи заказов, поступающих из Кафки.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class OrderEventConsumer {

    private static final String CONSUMER_GROUP_INITIAL_ORDER = "init-order-subscriber-group";
    private static final String CONSUMER_GROUP_INDEX_ORDER = "init-order-indexing-subscriber-group";
    private PersistentEntityRegistry persistentEntityRegistry;
    private ElasticRepository elasticRepository;

    @Inject
    public OrderEventConsumer(PersistentEntityRegistry registry, ReadSide readSide,
                              OrderEventsClient orderEventsClient, ElasticRepository elasticRepository) {
        register(registry, readSide);
        this.elasticRepository = elasticRepository;
        orderEventsClient.getTopicOrder()
                .subscribe()
                .withGroupId(CONSUMER_GROUP_INITIAL_ORDER)
                .atLeastOnce(Flow.<OrderEvent>create().<Done>mapAsync(1, this::insertOrder));
        orderEventsClient.getTopicOrder()
                .subscribe()
                .withGroupId(CONSUMER_GROUP_INDEX_ORDER)
                .atLeastOnce(Flow.<OrderEvent>create().<Done>mapAsync(1, this::indexOrder));
    }

    /**
     * Зарегистрировать persistent entity.
     * @param registry реестр для read-side
     * @param readSide read-side
     */
    protected void register(PersistentEntityRegistry registry, ReadSide readSide) {
        this.persistentEntityRegistry = registry;
        persistentEntityRegistry.register(OrderEntity.class);
    }

    /**
     * Сохранить данные заказа по событию кафки в кассандру.
     *
     * @param event - заказ в формате кафки
     * @return {@code CompletionStage<Done>}
     */
    private CompletionStage<Done> insertOrder(OrderEvent event) {
        if (event instanceof InitOrder) {
            Order order = Converters.KafkaToCassandra.toOrder((InitOrder) event);
            return persistentEntityRegistry
                    .refFor(OrderEntity.class, order.getId())
                    .ask(new InsertOrder(order))
                    .thenApply(insertOrderDone -> done());
        } else {
            return completedFuture(done());
        }
    }

    /**
     * Сохранить данные заказа по событию кафки в эластик.
     *
     * @param event - заказ в формате кафки
     * @return {@code CompletionStage<Done>}
     */
    private CompletionStage<Done> indexOrder(OrderEvent event) {
        if (event instanceof InitOrder) {
            return elasticRepository.storeOrder(
                    ru.shadewallcorp.itdepart.sampleService.elastic.Converters.KafkaToEs.toOrder((InitOrder) event));
        } else {
            return completedFuture(done());
        }
    }

}
