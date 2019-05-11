package io.gridgo.boot.support.scanners.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import org.reflections.Reflections;

import io.gridgo.boot.support.AnnotationScanner;
import io.gridgo.boot.support.LazyInitializer;
import io.gridgo.boot.support.annotations.AnnotationUtils;
import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.connector.support.config.ConnectorContextBuilder;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.support.Registry;

public class GatewayScanner implements AnnotationScanner, ClassResolver {

    @Override
    public void scanAnnotation(Reflections ref, GridgoContext context, List<LazyInitializer> lazyInitializers) {
        var gateways = ref.getTypesAnnotatedWith(Gateway.class);
        for (var gateway : gateways) {
            registerGateway(context, gateway, lazyInitializers);
        }
    }

    private void registerGateway(GridgoContext context, Class<?> gatewayClass, List<LazyInitializer> lazyInitializers) {
        var annotation = gatewayClass.getAnnotation(io.gridgo.boot.support.annotations.Gateway.class);
        var name = annotation.value().isEmpty() ? gatewayClass.getName() : annotation.value();

        var gateway = context.openGateway(name) //
                             .setAutoStart(annotation.autoStart());
        attachConnectors(context.getRegistry(), gatewayClass, gateway);
        var instance = resolveClass(gatewayClass, context);
        subscribeProcessor(context.getRegistry(), gatewayClass, gateway, instance);
        lazyInitializers.add(new LazyInitializer(gatewayClass, instance));
    }

    private void subscribeProcessor(Registry registry, Class<?> gatewayClass, GatewaySubscription gateway,
            Object instance) {
        var executionStrategy = extractExecutionStrategy(registry, gatewayClass);
        if (instance instanceof Processor) {
            gateway.subscribe((Processor) instance).using(executionStrategy);
        } else {
            subscribeProcessorMethods(gatewayClass, gateway, instance, executionStrategy);
        }
    }

    private void attachConnectors(Registry registry, Class<?> gatewayClass, GatewaySubscription gateway) {
        var connectors = gatewayClass.getAnnotationsByType(Connector.class);
        for (var connector : connectors) {
            var endpoint = registry.substituteRegistriesRecursive(connector.value());
            if (connector.builder().isBlank()) {
                gateway.attachConnector(endpoint);
            } else {
                var builder = registry.lookupMandatory(connector.builder(), ConnectorContextBuilder.class);
                gateway.attachConnector(endpoint, builder.build());
            }
        }
    }

    private void subscribeProcessorMethods(Class<?> gatewayClass, GatewaySubscription gateway, Object instance,
            ExecutionStrategy executionStrategy) {
        var methods = AnnotationUtils.findAllMethodsWithAnnotation(gatewayClass,
                io.gridgo.boot.support.annotations.Processor.class);
        for (var method : methods) {
            var staticMethod = Modifier.isStatic(method.getModifiers());
            gateway.subscribe((rc, gc) -> {
                try {
                    if (staticMethod)
                        method.invoke(null, rc, gc);
                    else
                        method.invoke(instance, rc, gc);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    rc.getDeferred().reject(e);
                }
            }).using(executionStrategy);
        }
    }

    private ExecutionStrategy extractExecutionStrategy(Registry registry, Class<?> gatewayClass) {
        var executionStrategy = gatewayClass.getAnnotation(io.gridgo.boot.support.annotations.ExecutionStrategy.class);
        if (executionStrategy == null)
            return null;
        return registry.lookupMandatory(executionStrategy.value(), ExecutionStrategy.class);
    }
}
