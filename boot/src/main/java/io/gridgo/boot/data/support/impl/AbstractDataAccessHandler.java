package io.gridgo.boot.data.support.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.boot.data.DataAccessHandler;
import io.gridgo.boot.data.PojoConverter;
import io.gridgo.boot.data.support.annotations.PojoMapper;
import io.gridgo.boot.data.support.annotations.SingleMapper;
import io.gridgo.connector.support.MessageProducer;
import io.gridgo.core.GridgoContext;
import io.gridgo.framework.support.Message;
import lombok.Data;

@Data
public abstract class AbstractDataAccessHandler<T extends Annotation> implements DataAccessHandler, PojoConverter {

    protected GridgoContext context;

    protected MessageProducer gateway;

    private final Class<? extends T> annotatedClass;

    public AbstractDataAccessHandler(Class<? extends T> annotatedClass) {
        this.annotatedClass = annotatedClass;
    }

    @Override
    public Promise<?, Exception> invoke(Object proxy, Method method, Object[] args) throws Throwable {
        var annotation = method.getAnnotation(annotatedClass);
        if (annotation == null) {
            return Promise.ofCause(new IllegalArgumentException(String.format("Method %s is not annotated with @%s",
                    proxy.getClass().getName(), method.getName(), annotatedClass.getSimpleName())));
        }
        var msgRequest = buildMessage(annotation, method, args);
        var msgResult = gateway.call(msgRequest);
        return msgResult.filterDone(r -> filterSingleMapper(method, r))
                .filterDone(r -> filterPojoMapper(method, r));
    }

    protected Object filterSingleMapper(Method method, Message result) {
        var annotation = method.getAnnotation(SingleMapper.class);
        if (annotation == null)
            return result;
        if(result.body().isArray()){
            var array = result.body().asArray();
            return array.isEmpty() ? null : array.get(0);
        }else {
            return result.body();
        }
    }

    protected Object filterPojoMapper(Method method, Object result) {
        var annotation = method.getAnnotation(PojoMapper.class);
        if (annotation == null)
            return result;
        var pojo = annotation.value();
        if (result instanceof Message)
            return toPojo(((Message) result).body(), pojo);
        return toPojo((BElement) result, pojo);
    }

    protected abstract Message buildMessage(T annotation, Method method, Object[] args);
}
