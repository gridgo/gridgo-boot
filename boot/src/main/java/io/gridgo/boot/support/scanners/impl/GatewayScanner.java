package io.gridgo.boot.support.scanners.impl;

import java.util.List;

import org.reflections.Reflections;

import io.gridgo.boot.support.AnnotationScanner;
import io.gridgo.boot.support.LazyInitializer;
import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.boot.support.annotations.Instrumenter;
import io.gridgo.boot.support.annotations.ProducerInstrumenter;
import io.gridgo.connector.support.config.ConnectorContextBuilder;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.framework.execution.ExecutionStrategy;
import io.gridgo.framework.execution.ExecutionStrategyInstrumenter;
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
        gateway.setProducerInstrumenter(extractProducerInstrumenter(context.getRegistry(), gatewayClass));
        subscribeProcessor(context.getRegistry(), gatewayClass, gateway, instance);
        lazyInitializers.add(new LazyInitializer(gatewayClass, instance));
    }

    private void subscribeProcessor(Registry registry, Class<?> gatewayClass, GatewaySubscription gateway,
            Object instance) {
        var executionStrategy = extractExecutionStrategy(registry, gatewayClass);
        var instrumenter = extractInstrumenter(registry, gatewayClass);
        if (instance instanceof Processor) {
            gateway.subscribe((Processor) instance) //
                   .using(executionStrategy) //
                   .instrumentWith(instrumenter);
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

    private ExecutionStrategy extractExecutionStrategy(Registry registry, Class<?> gatewayClass) {
        var executionStrategy = gatewayClass.getAnnotation(io.gridgo.boot.support.annotations.ExecutionStrategy.class);
        if (executionStrategy == null)
            return null;
        return registry.lookupMandatory(executionStrategy.value(), ExecutionStrategy.class);
    }

    private ExecutionStrategyInstrumenter extractInstrumenter(Registry registry, Class<?> gatewayClass) {
        var instrumenter = gatewayClass.getAnnotation(Instrumenter.class);
        if (instrumenter == null)
            return null;
        return registry.lookupMandatory(instrumenter.value(), ExecutionStrategyInstrumenter.class);
    }

    private io.gridgo.framework.execution.ProducerInstrumenter extractProducerInstrumenter(Registry registry,
            Class<?> gatewayClass) {
        var instrumenter = gatewayClass.getAnnotation(ProducerInstrumenter.class);
        if (instrumenter == null)
            return null;
        return registry.lookupMandatory(instrumenter.value(), io.gridgo.framework.execution.ProducerInstrumenter.class);
    }
}
