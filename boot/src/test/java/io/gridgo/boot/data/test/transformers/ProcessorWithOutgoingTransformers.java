package io.gridgo.boot.data.test.transformers;

import io.gridgo.boot.support.annotations.Connector;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.Processor;
import io.gridgo.core.support.RoutingContext;

@Gateway("outgoing")
@Connector(value = "test:", outgoingFormat = "json")
@Connector(value = "test:", outgoingFormat = "json", outgoingTransformers = { "trans1", "trans2" })
@Connector(value = "test:", outgoingTransformers = { "trans1" })
public class ProcessorWithOutgoingTransformers implements Processor {

    @Override
    public void process(RoutingContext rc, GridgoContext gc) {

    }
}
