package io.gridgo.boot.data.test.dataaccess.transformers;

import io.gridgo.framework.support.Message;

public interface Transformer {

    public Message transform(Message msg);
}
