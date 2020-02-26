package ru.shadewallcorp.itdepart.sampleService.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.taymyr.lagom.javadsl.openapi.OpenAPIService;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * Сервис поиска заказов.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public interface SampleService extends OpenAPIService {

    String SERVICE_NAME = "sampleService";

    /**
     * Получить карточку заказа по ID.
     * @param id ID
     * @return {@link ServiceCall}
     */
    ServiceCall<NotUsed, OrderCard> getOrderCard(Long id);

    /**
     * Получить список всех заказов по фильтру.
     * @return {@link ServiceCall}
     */
    ServiceCall<FindOrdersRequest, FindOrdersResponse> findOrders();

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(named(SERVICE_NAME)
                .withCalls(
                        restCall(Method.GET, "/sampleService/order/:id", this::getOrderCard),
                        restCall(Method.POST, "/sampleService/orders", this::findOrders))
                .withAutoAcl(true));
    }

}
