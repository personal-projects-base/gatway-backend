package com.smartverse.bridgebackend.config.gateway;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.smartverse.bridgebackend.config.mongo.ConnectionMongoDb;
import org.bson.Document;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Component
public class MongoRouteDefinitionRepository implements RouteDefinitionRepository {

    public static final String COLLECTION_NAME = "gateway_routes";

    private MongoCollection<Document> collection() {
        return ConnectionMongoDb.getInstance().getDatabase().getCollection(COLLECTION_NAME);
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.defer(() -> Flux.fromIterable(collection().find(eq("enabled", true))))
                .map(this::fromDocument)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.doOnNext(this::validate)
                .flatMap(definition -> Mono.fromRunnable(() -> collection().replaceOne(
                        eq("_id", definition.getId()), toDocument(definition, true),
                        new ReplaceOptions().upsert(true))))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> Mono.fromRunnable(() -> collection().deleteOne(eq("_id", id))))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public long count() {
        return collection().countDocuments();
    }

    public void upsert(RouteDefinition route) {
        validate(route);
        collection().replaceOne(eq("_id", route.getId()), toDocument(route, true),
                new ReplaceOptions().upsert(true));
    }

    Document toDocument(RouteDefinition route, boolean enabled) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (route.getMetadata() != null) {
            metadata.putAll(route.getMetadata());
        }
        return new Document("_id", route.getId())
                .append("uri", route.getUri().toString())
                .append("service", metadata.get("service"))
                .append("order", route.getOrder())
                .append("enabled", enabled)
                .append("predicates", route.getPredicates().stream().map(this::toDocument).toList())
                .append("filters", route.getFilters().stream().map(this::toDocument).toList())
                .append("metadata", new Document(metadata));
    }

    RouteDefinition fromDocument(Document source) {
        RouteDefinition route = new RouteDefinition();
        route.setId(source.getString("_id"));
        route.setUri(URI.create(source.getString("uri")));
        route.setOrder(source.getInteger("order", 0));
        route.setPredicates(readPredicates(source));
        route.setFilters(readFilters(source));
        Document metadata = source.get("metadata", Document.class);
        if (metadata != null) {
            route.setMetadata(new LinkedHashMap<>(metadata));
        }
        if (route.getMetadata() == null) {
            route.setMetadata(new LinkedHashMap<>());
        }
        String service = source.getString("service");
        if (StringUtils.hasText(service)) {
            route.getMetadata().put("service", service);
        }
        validate(route);
        return route;
    }

    private Document toDocument(PredicateDefinition definition) {
        return new Document("name", definition.getName())
                .append("args", mapDocument(definition.getArgs()));
    }

    private Document toDocument(FilterDefinition definition) {
        return new Document("name", definition.getName())
                .append("args", mapDocument(definition.getArgs()));
    }

    private Document mapDocument(Map<String, String> values) {
        Document document = new Document();
        if (values != null) {
            values.forEach(document::append);
        }
        return document;
    }

    private List<PredicateDefinition> readPredicates(Document source) {
        List<PredicateDefinition> result = new ArrayList<>();
        for (Document document : source.getList("predicates", Document.class, List.of())) {
            PredicateDefinition definition = new PredicateDefinition();
            definition.setName(document.getString("name"));
            definition.setArgs(readArgs(document));
            result.add(definition);
        }
        return result;
    }

    private List<FilterDefinition> readFilters(Document source) {
        List<FilterDefinition> result = new ArrayList<>();
        for (Document document : source.getList("filters", Document.class, List.of())) {
            FilterDefinition definition = new FilterDefinition();
            definition.setName(document.getString("name"));
            definition.setArgs(readArgs(document));
            result.add(definition);
        }
        return result;
    }

    private Map<String, String> readArgs(Document definition) {
        Document args = definition.get("args", Document.class);
        Map<String, String> result = new LinkedHashMap<>();
        if (args != null) {
            args.forEach((key, value) -> result.put(key, String.valueOf(value)));
        }
        return result;
    }

    private void validate(RouteDefinition route) {
        if (!StringUtils.hasText(route.getId())) {
            throw new IllegalArgumentException("Route id must not be empty");
        }
        if (route.getUri() == null) {
            throw new IllegalArgumentException("Route URI must not be empty: " + route.getId());
        }
    }
}
