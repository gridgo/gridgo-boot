package io.gridgo.boot.data.test.transformers;

import io.gridgo.boot.support.annotations.Component;
import io.gridgo.framework.support.Message;

@Component("nop")
public class NopTransformer implements Transformer {

    @Override
    public Message transform(Message msg) {
        return msg;
    }
}
