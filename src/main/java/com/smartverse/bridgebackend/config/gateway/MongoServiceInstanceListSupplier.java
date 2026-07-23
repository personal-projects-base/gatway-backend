package com.smartverse.bridgebackend.config.gateway;

import com.mongodb.client.MongoCollection;
import com.smartverse.bridgebackend.config.mongo.ConnectionMongoDb;
import org.bson.Document;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/** Reads load-balancer instances from the same gateway_routes collection. */
public class MongoServiceInstanceListSupplier implements ServiceInstanceListSupplier {
    private final String serviceId;

    public MongoServiceInstanceListSupplier(Environment environment) {
        this.serviceId = environment.getProperty("spring.cloud.loadbalancer.client.name", "");
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(load());
    }

    private List<ServiceInstance> load() {
        MongoCollection<Document> collection = ConnectionMongoDb.getInstance().getDatabase()
                .getCollection(MongoRouteDefinitionRepository.COLLECTION_NAME);
        Document route = collection.find(eq("service", serviceId)).first();
        if (route == null || !Boolean.TRUE.equals(route.getBoolean("enabled", true))) {
            return List.of();
        }
        return route.getList("instances", Document.class, List.of()).stream()
                .filter(instance -> instance.getBoolean("enabled", true))
                .map(this::instance)
                .toList();
    }

    private ServiceInstance instance(Document document) {
        URI uri = URI.create(document.getString("uri"));
        return new DefaultServiceInstance(
                serviceId + "-" + uri.getHost() + "-" + uri.getPort(),
                serviceId, uri.getHost(), uri.getPort(), "https".equalsIgnoreCase(uri.getScheme()));
    }
}
