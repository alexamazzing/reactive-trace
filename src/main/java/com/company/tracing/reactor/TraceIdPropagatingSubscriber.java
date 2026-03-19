package com.company.tracing.reactor;


import org.apache.logging.log4j.ThreadContext;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

public class TraceIdPropagatingSubscriber<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> delegate;

    public TraceIdPropagatingSubscriber(CoreSubscriber<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    @Override
    public void onSubscribe(Subscription s) {
        delegate.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        String traceId = currentContext().getOrDefault("traceId", null);
        if (traceId != null) {
            ThreadContext.put("traceId", traceId);
        } else {
            ThreadContext.remove("traceId");
        }
        delegate.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        delegate.onError(t);
    }

    @Override
    public void onComplete() {
        delegate.onComplete();
    }
}