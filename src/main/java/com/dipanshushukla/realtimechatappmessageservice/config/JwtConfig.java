package com.dipanshushukla.realtimechatappmessageservice.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jwt.proc.*;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final DiscoveryClient discoveryClient;

    @Bean
    public ConfigurableJWTProcessor<SecurityContext> jwtProcessor() throws Exception {

        List<ServiceInstance> instances = discoveryClient.getInstances("realtime-chatapp-auth-service");

        if (instances.isEmpty()) {
            throw new IllegalStateException("Auth service not found in Eureka");
        }

        String jwksUri = instances.get(0).getUri() + "/.well-known/jwks.json";

        JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(new URL(jwksUri)).build();

        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);

        processor.setJWSKeySelector(keySelector);

        return processor;
    }
}
