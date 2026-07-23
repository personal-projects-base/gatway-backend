package com.smartverse.bridgebackend.config.gateway;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class MongoLoadBalancerConfiguration {
    @Bean
    ServiceInstanceListSupplier serviceInstanceListSupplier(Environment environment) {
        return new MongoServiceInstanceListSupplier(environment);
    }
}
