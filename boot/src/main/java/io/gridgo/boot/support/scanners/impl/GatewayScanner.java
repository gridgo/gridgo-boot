package io.gridgo.boot.support.scanners.impl;

import org.reflections.Reflections;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.gridgo.boot.support.AnnotationScanner;
import io.gridgo.boot.support.LazyInitializer;
import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.boot.support.annotations.Instrumenter;
import io.gridgo.boot.support.annotations.ProducerInstrumenter;
import io.gridgo.connector.support.config.ConnectorContextBuilder;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.subscription.ConnectorAttachment;
import io.gridgo.core.support.subscription.GatewaySubscription;
import io.gridgo.core.support.transformers.MessageTransformer;
import io.gridgo.core.support.transformers.impl.FormattedDeserializeMessageTransformer;
import io.gridgo.core.support.transformers.impl.FormattedSerializeMessageTransformer;
import io.gridgo.core.support.transformers.impl.WrappedMessageTransformer;
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
            ConnectorAttachment attachment = null;
            if (connector.builder().isBlank()) {
                attachment = gateway.attachConnector(endpoint);
            } else {
                var builder = registry.lookupMandatory(connector.builder(), ConnectorContextBuilder.class);
                attachment = gateway.attachConnector(endpoint, builder.build());
            }
            MessageTransformer incomingTransformer = extractTransformer(
                    registry,
                    connector.incomingFormat(),
                    connector.incomingTransformers(), true);
            if (incomingTransformer != null) {
                attachment.transformIncomingWith(incomingTransformer);
            }
            MessageTransformer outgoingTransformer = extractTransformer(
                    registry,
                    connector.outgoingFormat(),
                    connector.outgoingTransformers(), false);
            if (outgoingTransformer != null) {
                attachment.transformOutgoingWith(outgoingTransformer);
            }
        }
    }

    private MessageTransformer extractTransformer(Registry registry, String format, String[] transformers, boolean incoming) {
        if (format.isEmpty() && transformers.length == 0)
            return null;
        var list = Arrays.stream(transformers) //
                         .map(transformer -> registry.lookupMandatory(transformer, MessageTransformer.class)) //
                         .collect(Collectors.toList());
        if (!format.isEmpty()) {
            var formatter = incoming ?
                    new FormattedDeserializeMessageTransformer(format)
                    : new FormattedSerializeMessageTransformer(format);
            list.add(formatter);
        }

        if (list.size() == 1)
            return list.get(0);
        return new WrappedMessageTransformer(list.toArray(new MessageTransformer[0]));
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
