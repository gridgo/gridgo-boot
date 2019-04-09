package io.gridgo.boot.support.injectors.impl;

import io.gridgo.boot.support.Injector;
import io.gridgo.boot.support.annotations.AnnotationUtils;
import io.gridgo.boot.support.annotations.ComponentInject;
import io.gridgo.core.GridgoContext;
import io.gridgo.utils.ObjectUtils;

public class ComponentInjector implements Injector {

    private GridgoContext context;

    public ComponentInjector(GridgoContext context) {
        this.context = context;
    }

    @Override
    public void inject(Class<?> gatewayClass, Object instance) {
        var fields = AnnotationUtils.findAllFieldsWithAnnotation(gatewayClass, ComponentInject.class);
        for (var field : fields) {
            var name = field.getName();
            var clazz = field.getType();
            var injectedKey = field.getAnnotation(ComponentInject.class).value();
            var component = lookupComponent(clazz, injectedKey);
            ObjectUtils.setValue(instance, name, component);
        }
    }

    protected Object lookupComponent(Class<?> clazz, String injectedKey) {
        var registry = context.getRegistry();
        var name = injectedKey.isEmpty() ? clazz.getName() : registry.substituteRegistriesRecursive(injectedKey);
        return registry.lookupMandatory(name, clazz);
    }
}
