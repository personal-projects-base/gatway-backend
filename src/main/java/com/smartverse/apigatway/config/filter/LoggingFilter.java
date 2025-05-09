package com.smartverse.apigatway.config.filter;

import com.smartverse.apigatway.model.LogFilter;
import com.smartverse.apigatway.service.log.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Autowired
    LogService logService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;

                    ServerHttpRequest request = exchange.getRequest();
                    ServerHttpResponse response = exchange.getResponse();

                    LogFilter log = new LogFilter();
                    log.setPath(request.getURI().getPath());
                    log.setMethod(request.getMethod().toString());
                    log.setStatus(response.getStatusCode().value());
                    log.setDuration(duration);
                    log.setTimestamp(LocalDateTime.now());

                    logService.saveLog(log);
                })
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
