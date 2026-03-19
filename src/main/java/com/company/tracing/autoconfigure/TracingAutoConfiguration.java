package com.company.tracing.autoconfigure;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import com.company.tracing.kafka.TracingKafkaSenderBeanPostProcessor;
import com.company.tracing.reactor.TraceIdPropagatingSubscriber;
import com.company.tracing.web.TraceIdWebFilter;



@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class TracingAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(TracingAutoConfiguration.class);

    @PostConstruct
    public void init() {
        log.info(">>> TracingAutoConfiguration LOADED <<<"); // ← должно появиться в логах
    }

    @Bean
    public TracingHooksInitializer tracingHooksInitializer() {
        return new TracingHooksInitializer();
    }

    static class TracingHooksInitializer {
        @PostConstruct
        public void init() {
            Hooks.onEachOperator("tracing", Operators.lift((scannable, subscriber) ->
                    new TraceIdPropagatingSubscriber<>(subscriber)
            ));
        }
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.web.server.WebFilter")
    @ConditionalOnMissingBean(TraceIdWebFilter.class)
    public TraceIdWebFilter traceIdWebFilter() {
        return new TraceIdWebFilter();
    }

    @Bean
    @ConditionalOnClass(name = "reactor.kafka.sender.KafkaSender")
    @ConditionalOnMissingBean(TracingKafkaSenderBeanPostProcessor.class)
    public TracingKafkaSenderBeanPostProcessor tracingKafkaSenderBeanPostProcessor() {
        return new TracingKafkaSenderBeanPostProcessor();
    }
}

