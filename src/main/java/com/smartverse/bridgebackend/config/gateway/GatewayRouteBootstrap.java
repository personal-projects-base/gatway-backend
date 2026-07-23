package com.smartverse.bridgebackend.config.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GatewayRouteBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayRouteBootstrap.class);

    private final MongoRouteDefinitionRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public GatewayRouteBootstrap(MongoRouteDefinitionRepository repository,
                                 ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        LOGGER.info("Gateway routes are loaded from MongoDB collection '{}'", MongoRouteDefinitionRepository.COLLECTION_NAME);

        
    }

    @Scheduled(fixedDelayString = "${gateway.routes.refresh-interval-ms:15000}")
    public void refreshRoutes() {
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }

}
