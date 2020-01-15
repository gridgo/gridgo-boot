package io.gridgo.boot.data.test.transformers;

import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.RoutingContext;

@Gateway("mixed")
@Connector(value = "test:", incomingFormat = "json", outgoingFormat = "json")
@Connector(value = "test:", incomingFormat = "json", incomingTransformers = { "trans1" },
        outgoingFormat = "json", outgoingTransformers = { "trans1", "trans2" })
@Connector(value = "test:", incomingTransformers = { "trans1" }, outgoingTransformers = { "trans1" })
public class ProcessorWithMixedTransformers implements Processor {

    @Override
    public void process(RoutingContext rc, GridgoContext gc) {

    }
}
