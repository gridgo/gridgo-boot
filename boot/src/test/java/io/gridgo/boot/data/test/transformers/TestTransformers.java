package io.gridgo.boot.data.test.transformers;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import java.util.ArrayList;

import io.gridgo.boot.support.scanners.impl.GatewayScanner;
import io.gridgo.core.impl.DefaultGridgoContextBuilder;
import io.gridgo.core.support.transformers.MessageTransformer;
import io.gridgo.core.support.transformers.impl.FormattedDeserializeMessageTransformer;
import io.gridgo.core.support.transformers.impl.FormattedSerializeMessageTransformer;
import io.gridgo.core.support.transformers.impl.WrappedMessageTransformer;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.SimpleRegistry;

public class TestTransformers {

    @Test
    public void testTransformers() {
        var registry = new SimpleRegistry()
                .register("trans1", new Type1MessageTransformer())
                .register("trans2", new Type2MessageTransformer());
        var scanner = new GatewayScanner();
        var context = new DefaultGridgoContextBuilder()
                .setName("test")
                .setRegistry(registry)
                .build();
        var ref = new Reflections("io.gridgo.boot.data.test.transformers");
        scanner.scanAnnotation(ref, context, new ArrayList<>());
        var incoming = context.findGatewayMandatory("incoming").getConnectorAttachments();
        var outgoing = context.findGatewayMandatory("outgoing").getConnectorAttachments();
        var mixed = context.findGatewayMandatory("mixed").getConnectorAttachments();
        var none = context.findGatewayMandatory("none").getConnectorAttachments();

        Assert.assertEquals(3, incoming.size());
        Assert.assertTrue(incoming.get(0).getIncomingTransformer() instanceof FormattedDeserializeMessageTransformer);
        Assert.assertTrue(incoming.get(1).getIncomingTransformer() instanceof WrappedMessageTransformer);
        Assert.assertTrue(incoming.get(2).getIncomingTransformer() instanceof Type1MessageTransformer);
        Assert.assertNull(incoming.get(0).getOutgoingTransformer());
        Assert.assertNull(incoming.get(1).getOutgoingTransformer());
        Assert.assertNull(incoming.get(2).getOutgoingTransformer());

        Assert.assertEquals(3, outgoing.size());
        Assert.assertTrue(outgoing.get(0).getOutgoingTransformer() instanceof FormattedSerializeMessageTransformer);
        Assert.assertTrue(outgoing.get(1).getOutgoingTransformer() instanceof WrappedMessageTransformer);
        Assert.assertTrue(outgoing.get(2).getOutgoingTransformer() instanceof Type1MessageTransformer);
        Assert.assertNull(outgoing.get(0).getIncomingTransformer());
        Assert.assertNull(outgoing.get(1).getIncomingTransformer());
        Assert.assertNull(outgoing.get(2).getIncomingTransformer());

        Assert.assertEquals(3, mixed.size());
        Assert.assertTrue(mixed.get(0).getIncomingTransformer() instanceof FormattedDeserializeMessageTransformer);
        Assert.assertTrue(mixed.get(1).getIncomingTransformer() instanceof WrappedMessageTransformer);
        Assert.assertTrue(mixed.get(2).getIncomingTransformer() instanceof Type1MessageTransformer);
        Assert.assertTrue(mixed.get(0).getOutgoingTransformer() instanceof FormattedSerializeMessageTransformer);
        Assert.assertTrue(mixed.get(1).getOutgoingTransformer() instanceof WrappedMessageTransformer);
        Assert.assertTrue(mixed.get(2).getOutgoingTransformer() instanceof Type1MessageTransformer);

        Assert.assertEquals(1, none.size());
        Assert.assertNull(none.get(0).getIncomingTransformer());
        Assert.assertNull(none.get(0).getOutgoingTransformer());
    }

    class Type1MessageTransformer implements MessageTransformer {

        @Override
        public Message transform(Message msg) {
            return msg;
        }
    }

    class Type2MessageTransformer implements MessageTransformer {

        @Override
        public Message transform(Message msg) {
            return msg;
        }
    }
}
