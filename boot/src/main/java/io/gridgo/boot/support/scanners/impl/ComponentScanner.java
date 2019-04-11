package io.gridgo.boot.support.scanners.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.reflections.Reflections;

import io.gridgo.boot.support.AnnotationScanner;
import io.gridgo.boot.support.LazyInitializer;
import io.gridgo.boot.support.annotations.Component;
import io.gridgo.boot.support.exceptions.InitializationException;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.support.exceptions.AmbiguousException;
import io.gridgo.framework.support.Registry;
import io.gridgo.framework.support.exceptions.BeanNotFoundException;

public class ComponentScanner implements AnnotationScanner {

    @Override
    public void scanAnnotation(Reflections ref, GridgoContext context, List<LazyInitializer> lazyInitializers) {
        var components = ref.getTypesAnnotatedWith(Component.class);
        for (var comp : components) {
            registerComponent(context, comp, lazyInitializers);
        }
    }

    private void registerComponent(GridgoContext context, Class<?> comp, List<LazyInitializer> lazyInitializers) {
        var annotation = comp.getAnnotation(Component.class);
        var name = annotation.value();
        try {
            var instance = resolveClass(comp, context);
            if (!name.isEmpty()) {
                context.getRegistry().register(name, instance);
            } else {
                context.getRegistry().register(comp.getName(), instance);
            }
            lazyInitializers.add(new LazyInitializer(comp, instance));
        } catch (IllegalArgumentException | SecurityException e) {
            throw new InitializationException("Cannot register processor", e);
        }
    }

    public Object resolveClass(Class<?> clazz, GridgoContext context) {
        try {
            var constructors = clazz.getConstructors();
            if (constructors.length > 1)
                throw new AmbiguousException("Only one constructor is allowed");
            var constructor = constructors[0];
            var params = Arrays.stream(constructor.getParameterTypes()) //
                               .map(type -> lookupForType(context, type)) //
                               .toArray(size -> new Object[size]);
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Object lookupForType(GridgoContext context, Class<?> type) {
        if (type == GridgoContext.class)
            return context;
        if (type == Registry.class)
            return context.getRegistry();
        var answer = context.getRegistry().lookupByType(type);
        if (answer == null)
            throw new BeanNotFoundException("Cannot find any bean with the required type " + type.getName());
        return answer;
    }
}
