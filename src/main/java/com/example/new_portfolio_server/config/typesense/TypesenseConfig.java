package com.example.new_portfolio_server.config.typesense;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.typesense.api.Client;
import org.typesense.resources.Node;


import java.time.Duration;
import java.util.List;

@Configuration
public class TypesenseConfig {
    @Value("${typesense.api-key}")
    private String apiKey;

    @Value("${typesense.host}")
    private String host;

    @Value("${typesense.port}")
    private String port;

    @Value("${typesense.protocol}")
    private String protocol;

    @Bean
    public Client typesenseClient() {
        Node node = new Node(protocol, host, port);
        org.typesense.api.Configuration configuration = new org.typesense.api.Configuration(
                List.of(node),
                Duration.ofSeconds(2),
                apiKey
        );
        return new Client(configuration);
    }
}
