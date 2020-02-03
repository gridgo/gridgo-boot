package io.gridgo.boot.data.test.dataaccess.data;

import org.joo.promise4j.Promise;

import java.util.List;

import io.gridgo.boot.data.jdbc.BindBatch;
import io.gridgo.boot.data.jdbc.JdbcProduce;
import io.gridgo.boot.data.support.annotations.DataAccess;
import io.gridgo.boot.data.support.annotations.PojoMapper;
import io.gridgo.boot.data.support.annotations.SingleMapper;
import io.gridgo.framework.support.Message;

@DataAccess(gateway = "mysql")
public interface UserDAO {

    @JdbcProduce("drop table if exists test_users")
    public Promise<Message, Exception> dropTable();

    @JdbcProduce("create table test_users (id int primary key, name varchar(255))")
    public Promise<Message, Exception> createTable();

    @JdbcProduce("insert into test_users (id, name) values (:1, :2)")
    public Promise<Message, Exception> add(int id, String name);

    @JdbcProduce(value = "insert into test_users (id, name) values (:id, :name)", batch = true)
    public Promise<Message, Exception> addBatch(@BindBatch List<User> users);

    @PojoMapper(User.class)
    @JdbcProduce(value = "select * from test_users where id = :1")
    public Promise<List<User>, Exception> find(int id);

    @PojoMapper(User.class)
    @SingleMapper
    @JdbcProduce(value = "select * from test_users where id = :1")
    public Promise<User, Exception> findSingle(int id);

    @PojoMapper(User.class)
    @JdbcProduce(value = "select * from test_users where id in (:1)")
    public Promise<List<User>, Exception> findByIds(List<Integer> ids);

    @PojoMapper(ModifiedUser.class)
    @SingleMapper
    @JdbcProduce(value = "select id as userId from test_users where id = :1")
    public Promise<ModifiedUser, Exception> findWithAlias(int id);

    @PojoMapper(User.class)
    @JdbcProduce(value = "select * from test_users")
    public Promise<List<User>, Exception> getAll();
}
