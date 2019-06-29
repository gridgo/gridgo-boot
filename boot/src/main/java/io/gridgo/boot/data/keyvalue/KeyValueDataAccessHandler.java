package io.gridgo.boot.data.keyvalue;

import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_DELETE;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_GET;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_GET_ALL;
import static io.gridgo.connector.keyvalue.KeyValueConstants.OPERATION_SET;

import java.lang.reflect.Method;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.boot.data.support.annotations.DataAccessSchema;
import io.gridgo.boot.data.support.impl.AbstractDataAccessHandler;
import io.gridgo.framework.support.Message;

@DataAccessSchema("keyvalue")
public class KeyValueDataAccessHandler extends AbstractDataAccessHandler<KeyValueProduce> {

    public KeyValueDataAccessHandler() {
        super(KeyValueProduce.class);
    }

    @Override
    protected Message buildMessage(KeyValueProduce annotation, Method method, Object[] args) {
        var op = parseOperation(annotation.value());
        var headers = BObject.of(OPERATION, op);
        var body = parseBody(annotation.value(), method, args);
        return Message.ofAny(headers, body);
    }

    private BElement parseBody(KeyValueOp op, Method method, Object[] args) {
        switch (op) {
        case DELETE:
        case GET:
            return parseBodyForGet(args);
        case SET:
            return parseBodyForSet(method, args);
        case GET_ALL:
            return null;
        }
        return null;
    }

    protected BElement parseBodyForGet(Object[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("GET/DELETE operation must have exactly 1 argument");
        }
        return BElement.ofAny(args[0]);
    }

    protected BElement parseBodyForSet(Method method, Object[] args) {
        if (args == null || (args.length != 1 && args.length != 2)) {
            throw new IllegalArgumentException("SET operation must have either 1 or 2 arguments");
        }
        if (args.length == 1) {
            return BElement.ofAny(args[0]);
        }
        var params = method.getParameters();
        String key = null;
        Object value = null;
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            var bind = param.getAnnotation(BindKey.class);
            if (bind != null) {
                key = args[i].toString();
            } else {
                value = args[i];
            }
        }
        if (key == null || value == null) {
            throw new IllegalArgumentException("Both key and value must be non-null for SET operation");
        }
        return BObject.of(key, value);
    }

    private String parseOperation(KeyValueOp value) {
        switch (value) {
        case GET:
            return OPERATION_GET;
        case GET_ALL:
            return OPERATION_GET_ALL;
        case SET:
            return OPERATION_SET;
        case DELETE:
            return OPERATION_DELETE;
        }
        return null;
    }
}
