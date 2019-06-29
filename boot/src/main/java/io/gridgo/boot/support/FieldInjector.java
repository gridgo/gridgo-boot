package io.gridgo.boot.support;

import io.gridgo.boot.data.DataAccessInjector;
import io.gridgo.boot.support.annotations.AnnotationUtils;
import io.gridgo.boot.support.annotations.PostConstruct;
import io.gridgo.boot.support.exceptions.InitializationException;
import io.gridgo.boot.support.injectors.impl.ComponentInjector;
import io.gridgo.boot.support.injectors.impl.GatewayInjector;
import io.gridgo.boot.support.injectors.impl.RegistryInjector;
import io.gridgo.core.GridgoContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class FieldInjector {

    private List<Injector> injectors;

    public FieldInjector(GridgoContext context) {
        this.injectors = Arrays.asList( //
                new RegistryInjector(context), //
                new GatewayInjector(context), //
                new ComponentInjector(context), //
                new DataAccessInjector(context));
    }

    public void injectFields(Class<?> gatewayClass, Object instance) {
        for (var injector : injectors) {
            injector.inject(gatewayClass, instance);
        }
        postConstructInitialize(gatewayClass, instance);
    }

    /**
     * Post constructor initialize
     * @param gatewayClass The component class
     * @param instance The instance of gatewayClass
     */
    private void postConstructInitialize(Class<?> gatewayClass, Object instance) {
        var postConstructs = AnnotationUtils.findAllMethodsWithAnnotation(gatewayClass, PostConstruct.class);
        if (postConstructs.size() >= 2) {
            throw new InitializationException("Only one PostConstruct method in a component be used");
        } else if (postConstructs.size() == 1) {
            var postConstruct = postConstructs.get(0);
            try {
                if (Modifier.isStatic(postConstruct.getModifiers())) {
                    postConstruct.invoke(null);
                } else {
                    postConstruct.invoke(instance);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new InitializationException("Cannot initialize the PostConstruct", e);
            }
        }
    }
}
