package ru.shadewallcorp.itdepart.sampleService;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.internal.javadsl.persistence.jdbc.JavadslJdbcOffsetStore;
import com.lightbend.lagom.internal.javadsl.persistence.jdbc.SlickProvider;
import com.lightbend.lagom.internal.persistence.jdbc.SlickOffsetStore;
import com.lightbend.lagom.javadsl.persistence.jdbc.GuiceSlickProvider;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import org.taymyr.lagom.elasticsearch.document.ElasticDocument;
import org.taymyr.lagom.elasticsearch.search.ElasticSearch;
import play.api.libs.ws.WSClient;
import play.api.libs.ws.ahc.AsyncHttpClientProvider;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import ru.shadewallcorp.itdepart.sampleService.api.SampleService;
import ru.shadewallcorp.itdepart.sampleService.application.OrderEventConsumer;
import ru.shadewallcorp.itdepart.sampleService.application.OrderEventsClient;
import ru.shadewallcorp.itdepart.sampleService.application.SampleServiceImpl;
import ru.shadewallcorp.itdepart.sampleService.elastic.ElasticRepository;
import ru.shadewallcorp.itdepart.sampleService.persistence.readside.OrderRepository;

/**
 * Order module.
 * https://github.com/lagom/lagom-recipes/blob/master/mixed-persistence/mixed-persistence-java-sbt/hello-impl/src/main/java/com/lightbend/
 *   lagom/recipes/mixedpersistence/hello/impl/HelloModule.java
 *
 * @author Dilvish {@literal <grant.all.on.any.table@gmail.com>}
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(SampleService.class, SampleServiceImpl.class);
        bind(SlickProvider.class).toProvider(GuiceSlickProvider.class);
        bind(SlickOffsetStore.class).to(JavadslJdbcOffsetStore.class);
        bind(ElasticRepository.class).asEagerSingleton();
        bindClient(ElasticDocument.class);
        bindClient(ElasticSearch.class);
        bind(OrderEventConsumer.class).asEagerSingleton();
        bind(OrderRepository.class).asEagerSingleton();
        bindClient(OrderEventsClient.class);
        // Logging of outgoing requests
        bind(AsyncHttpClient.class).toProvider(AsyncHttpClientProvider.class);
        bind(WSClient.class).toProvider(ConfiguredAhcWSClientProvider.class);

    }
}
