package io.gridgo.boot.support.scanners.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.reflections.Reflections;

import io.gridgo.boot.support.AnnotationScanner;
import io.gridgo.boot.support.LazyInitializer;
import io.gridgo.boot.support.annotations.Component;
import io.gridgo.boot.support.exceptions.InitializationException;
import io.gridgo.core.GridgoContext;

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
            var instance = comp.getConstructor().newInstance();
            if (!name.isEmpty()) {
                context.getRegistry().register(name, instance);
            } else {
                context.getRegistry().register(comp.getName(), instance);
            }
            lazyInitializers.add(new LazyInitializer(comp, instance));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new InitializationException("Cannot register processor", e);
        }
    }
}
