package com.smartverse.bridgebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import com.smartverse.bridgebackend.config.gateway.MongoLoadBalancerConfiguration;

@SpringBootApplication
@LoadBalancerClients(defaultConfiguration = MongoLoadBalancerConfiguration.class)
public class BridgeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BridgeBackendApplication.class, args);
	}

}
