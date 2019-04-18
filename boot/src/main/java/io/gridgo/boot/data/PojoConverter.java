package io.gridgo.boot.data;

import java.util.ArrayList;
import java.util.List;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;

public interface PojoConverter {

    public default Object toPojo(BElement body, Class<?> pojo) {
        if (body == null)
            return null;
        if (body.isObject())
            return toPojoObject(pojo, body.asObject());
        if (body.isArray())
            return toPojoArray(pojo, body.asArray());
        if (body.isReference())
            return toPojoReference(pojo, body);
        throw new IllegalArgumentException(String.format("Result of type %s cannot be casted to %s", //
                body.getType().name(), pojo.getClass().getName()));
    }

    public default Object toPojoReference(Class<?> pojo, BElement body) {
        if (pojo.isInstance(body.asReference()))
            return body.asReference().getReference();
        throw new IllegalArgumentException(String.format("Result of type %s cannot be casted to %s", //
                body.asReference().getReference().getClass().getName(), //
                pojo.getClass().getName()));
    }

    public default Object toPojoObject(Class<?> pojo, BObject body) {
        return body.toPojo(pojo);
    }

    public default List<?> toPojoArray(Class<?> pojo, BArray body) {
        var list = new ArrayList<>();
        for (int i = 0; i < body.size(); i++) {
            var e = body.get(i).asObject();
            list.add(e.toPojo(pojo));
        }
        return list;
    }
}
