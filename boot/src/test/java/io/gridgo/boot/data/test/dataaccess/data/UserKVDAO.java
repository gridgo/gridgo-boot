package io.gridgo.boot.data.test.dataaccess.data;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BObject;
import io.gridgo.boot.data.keyvalue.BindKey;
import io.gridgo.boot.data.keyvalue.KeyValueOp;
import io.gridgo.boot.data.keyvalue.KeyValueProduce;
import io.gridgo.boot.data.support.annotations.DataAccess;
import io.gridgo.boot.data.support.annotations.PojoMapper;

@DataAccess(gateway = "rocksdb")
public interface UserKVDAO {

    @PojoMapper(User.class)
    @KeyValueProduce(KeyValueOp.GET)
    public Promise<User, Exception> find(String userId);

    @PojoMapper(User.class)
    @KeyValueProduce(KeyValueOp.SET)
    public Promise<User, Exception> add(@BindKey String userId, BObject user);
}
