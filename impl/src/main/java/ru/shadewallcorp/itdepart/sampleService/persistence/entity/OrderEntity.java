package ru.shadewallcorp.itdepart.sampleService.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityCommand.InsertOrder;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityCommand.InsertOrderDone;
import ru.shadewallcorp.itdepart.sampleService.persistence.entity.OrderEntityEvent.OrderEntityInserted;

import java.util.Optional;

/**
 * Агрегат заказов для работы с кассандрой.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 * @see <a href="https://www.lagomframework.com/documentation/1.4.x/java/PersistentEntity.html">Lagom Persistent Entity</a>
 * @see <a href="https://www.lagomframework.com/documentation/1.4.x/java/PersistentEntityCassandra.html">Cassandra Persistent Entities</a>
 */
public class OrderEntity extends PersistentEntity<OrderEntityCommand, OrderEntityEvent, OrderEntityState> {

    /**
     * Инициализировать поведение для персистентной сущности заказа.
     * Поведение состоит из текущего состояния и функций для обработки входящих команд и событий персистентной сущности.
     *
     * @param snapshotState снепшот, по которому восстанавливается поведение
     * @return поведение персистентной сущности {@code Behavior}
     */
    @Override
    public Behavior initialBehavior(Optional<OrderEntityState> snapshotState) {
        if (snapshotState.isPresent() && !snapshotState.get().isEmpty()) {
            return initialBehaviorForExistent(snapshotState.get());
        } else {
            return initialBehaviorForNew();
        }
    }

    /**
     * Определить поведение для новой персистентной сущности.
     *
     * @return {@code Behavior}
     */
    private Behavior initialBehaviorForNew() {
        // Behavior это состояние, обработчики команд и обработчики событий.
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(OrderEntityState.EMPTY);
        // Обработчики команд - для входящих команд.
        // Обработчик команд должен вернуть (при необходимости) событие для записи персистентной сущности.
        behaviorBuilder.setCommandHandler(InsertOrder.class, this::insertOrderCommandHandler);
        //--
        behaviorBuilder.setEventHandlerChangingBehavior(OrderEntityInserted.class,
            event -> initialBehaviorForExistent(new OrderEntityState(event.getOrder())));
        return behaviorBuilder.build();
    }

    /**
     * Определить поведение для существующей персистентной сущности.
     *
     * @param orderEntityState состояние персистентной сущности
     * @return {@code Behavior}
     */
    private Behavior initialBehaviorForExistent(OrderEntityState orderEntityState) {
        // Восстановление сущности из снэпшота
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(orderEntityState);
        // запретить повторную вставку
        behaviorBuilder.setCommandHandler(InsertOrder.class, (command, context) -> {
            context.commandFailed(
                    new OrderAlreadyExistsException(entityId(), orderEntityState.getOrder().getId()));
            return context.done();
        });
        return behaviorBuilder.build();
    }

    private Persist<OrderEntityEvent> insertOrderCommandHandler(InsertOrder command, CommandContext<InsertOrderDone> context) {
        String id = command.getOrder().getId();
        if (id != null && !"".equals(id)) {
            return context.thenPersist(
                new OrderEntityInserted(command.getOrder()),
                event -> context.reply(new InsertOrderDone(entityId(), event.getOrder())));
        } else {
            context.commandFailed(new OrderMustHaveIdException(entityId()));
            return context.done();
        }
    }

    /**
     * Валидация однократной всавки заказа.
     */
    public class OrderAlreadyExistsException extends Throwable implements Jsonable {
        @JsonCreator
        public OrderAlreadyExistsException(String msg) {
            super(msg);
        }

        OrderAlreadyExistsException(String id, String entityId) {
            super(String.format("Order with id=%s already exists and can`t be inserted twice (entityId=%s)", id, entityId));
        }

    }

    /**
     * Валидация идентификатора заказа.
     */
    public class OrderMustHaveIdException extends Throwable implements Jsonable {
        @JsonCreator
        OrderMustHaveIdException(String entityId) {
            super(String.format("Order must have non-empty id (entityId=%s)", entityId));
        }
    }

}
