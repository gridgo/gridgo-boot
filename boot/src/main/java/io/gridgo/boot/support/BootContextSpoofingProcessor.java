package io.gridgo.boot.support;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.gridgo.bean.BObject;
import io.gridgo.boot.data.support.annotations.DataAccessInject;
import io.gridgo.boot.support.annotations.ComponentInject;
import io.gridgo.boot.support.annotations.GatewayInject;
import io.gridgo.boot.support.annotations.RegistryInject;
import io.gridgo.core.Processor;
import io.gridgo.core.support.impl.ContextSpoofingProcessor;

public class BootContextSpoofingProcessor extends ContextSpoofingProcessor {

    @Override
    protected BObject spoofProcessor(Processor processor) {
        var result = super.spoofProcessor(processor);
        var injections = spoofInjection(processor);
        if (injections.isEmpty())
            return result;
        return result.setAny("injections", injections);
    }

    private List<?> spoofInjection(Object processor) {
        return Arrays.stream(processor.getClass().getDeclaredFields()) //
                     .map(f -> mapInjection(processor, f)) //
                     .filter(Objects::nonNull) //
                     .collect(Collectors.toList());
    }

    private BObject mapInjection(Object processor, Field f) {
        var type = f.getType().getName();
        var result = BObject.of("fieldName", f.getName()) //
                            .setAny("fieldType", type);
        var gatewayInject = f.getAnnotation(GatewayInject.class);
        if (gatewayInject != null) {
            return result.setAny("type", "gateway") //
                         .setAny("target", gatewayInject.value().isEmpty() ? gatewayInject.clazz().getName()
                                 : gatewayInject.value());
        }
        var registryInject = f.getAnnotation(RegistryInject.class);
        if (registryInject != null) {
            return result.setAny("type", "registry") //
                         .setAny("target", registryInject.value());
        }
        var componentInject = f.getAnnotation(ComponentInject.class);
        if (componentInject != null) {
            return result.setAny("type", "component") //
                         .setAny("target", type);
        }
        var dataAccessInject = f.getAnnotation(DataAccessInject.class);
        if (dataAccessInject != null) {
            return result.setAny("type", "dataAccess") //
                         .setAny("target", type);
        }
        return null;
    }
}
