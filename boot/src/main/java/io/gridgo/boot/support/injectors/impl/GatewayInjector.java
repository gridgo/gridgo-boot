package io.gridgo.boot.support.injectors.impl;

import io.gridgo.boot.support.Injector;
import io.gridgo.boot.support.annotations.AnnotationUtils;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.boot.support.annotations.GatewayInject;
import io.gridgo.boot.support.exceptions.InitializationException;
import io.gridgo.boot.support.exceptions.InjectException;
import io.gridgo.core.GridgoContext;
import io.gridgo.utils.ObjectUtils;

public class GatewayInjector implements Injector {

    private GridgoContext context;

    public GatewayInjector(GridgoContext context) {
        this.context = context;
    }

    @Override
    public void inject(Class<?> gatewayClass, Object instance) {
        var fields = AnnotationUtils.findAllFieldsWithAnnotation(gatewayClass, GatewayInject.class);
        for (var field : fields) {
            var name = field.getName();
            var annotation = field.getAnnotation(GatewayInject.class);
            var injectedKey = annotation.value();
            if (injectedKey.isEmpty() && annotation.clazz() != void.class) {
                injectedKey = resolveWithClass(annotation.clazz());
            }
            if (injectedKey.isEmpty()) {
                throw new InitializationException(
                        String.format("Cannot inject gateway to field [%s.%s]. The Gateway name must be specified",
                                gatewayClass.getName(), field.getName()));
            }
            var gateway = context.findGatewayMandatory(injectedKey);
            ObjectUtils.setValue(instance, name, gateway);
        }
    }

    private String resolveWithClass(Class<?> clazz) {
        var annotation = clazz.getAnnotation(Gateway.class);
        if (annotation == null) {
            throw new InjectException(String.format(
                    "Cannot inject gateway with class %s. The class is not annotated with @Gateway", clazz.getName()));
        }
        return annotation.value().isEmpty() ? clazz.getName() : annotation.value();
    }
}
