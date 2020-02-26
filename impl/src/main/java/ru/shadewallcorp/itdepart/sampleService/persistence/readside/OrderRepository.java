package ru.shadewallcorp.itdepart.sampleService.persistence.readside;

import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.jpa.JpaSession;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
@Singleton
public class OrderRepository {
    private final JpaSession jpaSession;

    @Inject
    public OrderRepository(JpaSession jpaSession, ReadSide readSide) {
        this.jpaSession = jpaSession;
        readSide.register(OrderReadSideProcessor.class);
    }

    /**
     * Поиск заказа по id.
     *
     * @param id id заказа
     * @return {@link Order}
     */
    public CompletionStage<Order> find(String id) {
        return jpaSession.withTransaction(em -> em.find(Order.class, id));
    }

}
