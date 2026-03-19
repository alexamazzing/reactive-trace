package com.company.tracing.kafka;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import reactor.kafka.sender.KafkaSender;

@Component
public class TracingKafkaSenderBeanPostProcessor implements BeanPostProcessor {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof KafkaSender<?, ?> sender) {
            return new TracingKafkaSender(sender);
        }
        return bean;
    }
}
