package ru.shadewallcorp.itdepart.sampleService.application;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import ru.shadewallcorp.itdepart.sampleService.topic.OrderEvents;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.topic;

/**
 * Order topic service impl.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public interface OrderEventsClient extends OrderEvents, Service {

    @Override
    default Descriptor descriptor() {
        return named("order-events-topic-client")
                .withTopics(topic(TOPIC_ORDER, this::getTopicOrder))
                .withAutoAcl(true);
    }

}
