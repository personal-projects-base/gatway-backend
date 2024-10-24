package com.smartverse.api_gatway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class GatwayConfi {


    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("front-end-route", r -> r.path("/app/**")
                        .uri("http://localhost:4200")) // URL do front-end
                .build();
    }
}
