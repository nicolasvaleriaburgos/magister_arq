package com.cl.publisher.online.endpointsservice.configurations;

import org.apache.http.HttpStatus;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cl.publisher.online.endpointsservice.dto.response.ResponseServicesDto;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    public static class Config {
        // empty class as I don't need any particular configuration
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

            String[] parts = authHeader.split(" ");

            if (parts.length != 2 || !"Bearer".equals(parts[0])) {
                exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return webClientBuilder.build()
                    .get()
                    .uri("lb://auth-service:8202/auth/validateToken")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().isError()) {
                            return clientResponse.bodyToMono(Object.class);
                        }
                        return clientResponse.bodyToMono(ResponseServicesDto.class);
                    }).flatMap(dto -> {
                        if (dto.getClass() != ResponseServicesDto.class) {
                            exchange.getResponse().setRawStatusCode(HttpStatus.SC_UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    });
        };
    }
}
