package io.gridgo.boot.data.test.transformers;

import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.RoutingContext;

@Gateway("none")
@Connector(value = "test:")
public class ProcessorWithoutTransformers implements Processor {

    @Override
    public void process(RoutingContext rc, GridgoContext gc) {

    }
}
