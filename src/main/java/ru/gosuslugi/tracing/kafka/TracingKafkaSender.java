package ru.gosuslugi.tracing.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaOutbound;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.kafka.sender.TransactionManager;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class TracingKafkaSender<K, V> implements KafkaSender<K, V> {

    private static final String TRACE_ID_HEADER = "X-B3-TraceId";
    private final KafkaSender<K, V> delegate;

    public TracingKafkaSender(KafkaSender<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> Flux<SenderResult<T>> send(Publisher<? extends SenderRecord<K, V, T>> records) {
        var enriched = Flux.from(records)
                .flatMap(record -> Mono.deferContextual(ctx -> {
                    ctx.getOrEmpty("traceId").ifPresent(traceId ->
                            record.headers().add(
                                    TRACE_ID_HEADER,
                                    traceId.toString().getBytes(StandardCharsets.UTF_8)
                            )
                    );
                    return Mono.just(record);
                }));

        return delegate.send(enriched);
    }

    @Override
    public <T> Flux<Flux<SenderResult<T>>> sendTransactionally(
            Publisher<? extends Publisher<? extends SenderRecord<K, V, T>>> records) {
        return delegate.sendTransactionally(records);
    }

    @Override
    public TransactionManager transactionManager() {
        return delegate.transactionManager();
    }

    @Override
    public KafkaOutbound<K, V> createOutbound() {
        return delegate.createOutbound();
    }

    @Override
    public <T> Mono<T> doOnProducer(Function<Producer<K, V>, ? extends T> function) {
        return delegate.doOnProducer(function);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
