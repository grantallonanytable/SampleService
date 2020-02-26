package ru.shadewallcorp.itdepart.sampleService.application;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.typesafe.config.Config;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersRequest;
import ru.shadewallcorp.itdepart.sampleService.api.FindOrdersResponse;
import ru.shadewallcorp.itdepart.sampleService.api.OrderCard;
import ru.shadewallcorp.itdepart.sampleService.api.SampleService;
import ru.shadewallcorp.itdepart.sampleService.elastic.ElasticRepository;

import javax.inject.Inject;

/**
 * Order service implementation.
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class SampleServiceImpl extends AbstractOpenAPIService implements SampleService {

    private ElasticRepository elasticRepository;

    @Inject
    public SampleServiceImpl(Config config, ElasticRepository elasticRepository) {
        super(config);
        this.elasticRepository = elasticRepository;
    }

    /**
     * Получить карточку заказа по ID.
     * @return {@link ServiceCall}
     */
    @Override
    public ServiceCall<NotUsed, OrderCard> getOrderCard(Long id) {
        return NotUsed -> elasticRepository.findOrderCards(id);
    }

    /**
     * Получить список всех заказов.
     * @return {@link ServiceCall}
     */
    @Override
    public ServiceCall<FindOrdersRequest, FindOrdersResponse> findOrders() {
        return findOrdersRequest  -> elasticRepository.findOrders(findOrdersRequest);
    }

}
