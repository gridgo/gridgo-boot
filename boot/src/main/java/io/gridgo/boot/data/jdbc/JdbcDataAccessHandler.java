package io.gridgo.boot.data.jdbc;

import java.lang.reflect.Method;

import io.gridgo.bean.BObject;
import io.gridgo.boot.data.support.annotations.DataAccessSchema;
import io.gridgo.boot.data.support.impl.AbstractDataAccessHandler;
import io.gridgo.framework.support.Message;

@DataAccessSchema("jdbc")
public class JdbcDataAccessHandler extends AbstractDataAccessHandler<JdbcProduce> {

    public JdbcDataAccessHandler() {
        super(JdbcProduce.class);
    }

    @Override
    protected Message buildMessage(JdbcProduce annotation, Method method, Object[] args) {
        var headers = BObject.ofEmpty();
        if (args != null) {
            var params = method.getParameters();
            for (int i = 0; i < args.length; i++) {
                var param = params[i];
                var bind = param.getAnnotation(Bind.class);
                if (bind != null) {
                    headers.setAny(bind.value(), args[i]);
                } else {
                    headers.setAny((i + 1) + "", args[i]);
                }
            }
        }
        var query = context.getRegistry().substituteRegistriesRecursive(annotation.value());
        return Message.ofAny(headers, query);
    }
}
