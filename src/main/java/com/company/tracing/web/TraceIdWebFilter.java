package com.company.tracing.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdWebFilter implements WebFilter {

    private static final String TRACE_ID_HEADER = "X-B3-TraceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = Optional
                .ofNullable(exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER))
                .orElseGet(() -> UUID.randomUUID().toString().replace("-", ""));

        exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);

        return chain.filter(exchange)
                .contextWrite(Context.of("traceId", traceId));
    }
}
