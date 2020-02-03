package io.gridgo.boot.data.jdbc;

import java.lang.reflect.Method;
import java.util.Collection;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.boot.data.support.annotations.DataAccessSchema;
import io.gridgo.boot.data.support.impl.AbstractDataAccessHandler;
import io.gridgo.framework.support.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataAccessSchema("jdbc")
public class JdbcDataAccessHandler extends AbstractDataAccessHandler<JdbcProduce> {

    public JdbcDataAccessHandler() {
        super(JdbcProduce.class);
    }

    @Override
    protected Message buildMessage(JdbcProduce annotation, Method method, Object[] args) {
        var headers = BObject.ofEmpty();
        if (args != null) {
            if (annotation.batch()) {
                bindBatch(method, args, headers);
            } else {
                bindSingle(method, args, headers);
            }
        }
        var query = context.getRegistry().substituteRegistriesRecursive(annotation.value());
        log.debug("Query: {}, params: {}", query, headers);
        return Message.ofAny(headers, query);
    }

    private void bindBatch(Method method, Object[] args, BObject headers) {
        var data = BArray.ofEmpty();
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            var bind = param.getAnnotation(BindBatch.class);
            if (bind != null) {
                for (var obj : (Collection<?>) args[i]) {
                    data.add(BObject.ofPojo(obj));
                }
            } else {
                data.add(BObject.ofPojo(args[i]));
            }
        }
        headers.setAny("JDBC_BatchData", data)
               .setAny("JDBC_IsBatch", true);
    }

    private void bindSingle(Method method, Object[] args, BObject headers) {
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
}
