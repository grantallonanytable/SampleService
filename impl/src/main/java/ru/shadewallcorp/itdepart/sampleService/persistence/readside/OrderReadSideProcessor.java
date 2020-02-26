package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.jpa.JpaReadSide;
import org.pcollections.PSequence;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityEvent;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Read-side для персистентной сущности заказа в Cassandra.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class OrderReadSideProcessor extends ReadSideProcessor<OrderEntityEvent> {

    private final JpaReadSide jpaReadSide;

    @Inject
    public OrderReadSideProcessor(JpaReadSide jpaReadSide) {
        this.jpaReadSide = jpaReadSide;
    }

    /**
     * Получить список тегов событий персистентной сущности заказа.
     * @return список тегов
     */
    @Override
    public PSequence<AggregateEventTag<OrderEntityEvent>> aggregateTags() {
        return OrderEntityEvent.TAG.allTags();
    }

    /**
     * Получить хендлер read-side для персистентной сущности.
     * @return хендлер
     */
    @Override
    public ReadSideHandler<OrderEntityEvent> buildHandler() {
        return jpaReadSide.<OrderEntityEvent>builder("order-read-side")
            .setEventHandler(OrderEntityEvent.OrderEntityInserted.class, this::createOrder)
            .build();
    }

    private void createOrder(EntityManager em, OrderEntityEvent.OrderEntityInserted event) {
        Order order = Converters.CassandraToJpa.toOrder(event.getOrder());
        em.persist(order);
    }

}
