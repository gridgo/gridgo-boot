package io.gridgo.boot.data.test.transformers;

import io.gridgo.framework.support.Message;

public interface Transformer {

    public Message transform(Message msg);
}
