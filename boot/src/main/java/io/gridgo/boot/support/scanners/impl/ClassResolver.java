package io.gridgo.boot.support.scanners.impl;

import java.lang.reflect.Parameter;
import java.util.Arrays;

import io.gridgo.boot.support.annotations.RegistryInject;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.exceptions.AmbiguousException;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.exceptions.BeanNotFoundException;

public interface ClassResolver {

    public default Object resolveClass(Class<?> clazz, GridgoContext context) {
        try {
            var constructors = clazz.getConstructors();
            if (constructors.length > 1)
                throw new AmbiguousException("Only one constructor is allowed");
            var constructor = constructors[0];
            var params = Arrays.stream(constructor.getParameters()) //
                    .map(type -> lookupForType(context, type)) //
                    .toArray(size -> new Object[size]);
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException("Cannot resolve class " + clazz, e);
        }
    }

    public default Object lookupForType(GridgoContext context, Parameter param) {
        var type = param.getType();
        if (type == GridgoContext.class)
            return context;
        if (type == Registry.class)
            return context.getRegistry();
        var annotation = param.getAnnotation(RegistryInject.class);
        if (annotation != null) {
            return context.getRegistry().lookupMandatory(annotation.value(), param.getType());
        }
        throw new BeanNotFoundException("Cannot find any bean with the required type " + param.getType());
    }
}
