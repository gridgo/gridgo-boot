package io.gridgo.boot.data.test.processors;

import io.gridgo.boot.data.test.data.UserDomainService;
import io.gridgo.boot.data.test.transformers.Transformer;
import io.gridgo.boot.support.annotations.ComponentInject;
import io.gridgo.boot.support.annotations.Gateway;
import io.gridgo.core.GridgoContext;
import io.gridgo.core.impl.AbstractProcessor;
import io.gridgo.core.support.RoutingContext;
import io.gridgo.framework.support.Message;
import lombok.Setter;

@Setter
@Gateway("test")
public class TestProcessor extends AbstractProcessor {

    @ComponentInject
    private UserDomainService userService;

    @ComponentInject("${transformer}")
    private Transformer transformer;

    @Override
    public void process(RoutingContext rc, GridgoContext gc) {
        userService.createAndSaveUser() //
                   .<Message, Exception>filterDone(transformer::transform) //
                   .forward(rc.getDeferred());
    }
}
